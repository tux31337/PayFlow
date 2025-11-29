package com.truvis.stock.application.event;

import com.truvis.stock.domain.timescale.StockPriceHistory;
import com.truvis.stock.repository.timescale.StockPriceHistoryRepository;
import com.truvis.stock.domain.event.StockPriceUpdateEvent;
import com.truvis.stock.infrastructure.sse.SseEmitterManager;
import com.truvis.stock.model.StockPriceUpdateResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * 주식 가격 변경 이벤트 리스너
 * - 도메인 이벤트를 수신하여 SSE로 전송
 * - 히스토리 DB 저장 (배치)
 */
@Slf4j
@Component
public class StockPriceUpdateEventListener {
    
    private final SseEmitterManager sseEmitterManager;
    private final StockPriceHistoryRepository historyRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final TransactionTemplate timescaleTransactionTemplate;
    
    @PersistenceContext(unitName = "timescale")
    private EntityManager timescaleEntityManager;
    
    public StockPriceUpdateEventListener(
            SseEmitterManager sseEmitterManager,
            StockPriceHistoryRepository historyRepository,
            RedisTemplate<String, String> redisTemplate,
            @Qualifier("timescaleTransactionManager") PlatformTransactionManager timescaleTransactionManager
    ) {
        this.sseEmitterManager = sseEmitterManager;
        this.historyRepository = historyRepository;
        this.redisTemplate = redisTemplate;
        this.timescaleTransactionTemplate = new TransactionTemplate(timescaleTransactionManager);
    }
    
    /**
     * Redis 키 접두사
     */
    private static final String REDIS_PRICE_PREFIX = "stock:price:";
    private static final long REDIS_PRICE_TTL_HOURS = 1;  // 1시간 TTL
    
    /**
     * 배치 저장용 버퍼
     * - 메모리에 임시 저장 후 5초마다 일괄 저장
     */
    private final Queue<StockPriceHistory> buffer = new ConcurrentLinkedQueue<>();
    
    /**
     * 가격 변경 이벤트 처리
     * - 비동기로 처리하여 이벤트 발행자 차단 방지
     */
    @Async("stockExecutor")
    @EventListener
    public void handleStockPriceUpdate(StockPriceUpdateEvent event) {
        try {
            String stockCode = event.getStockCode().getValue();
            
            // 1. Redis에 최신 가격 저장 (빠른 조회를 위해)
            String redisKey = REDIS_PRICE_PREFIX + stockCode;
            redisTemplate.opsForValue().set(
                    redisKey,
                    String.valueOf(event.getCurrentPrice()),
                    REDIS_PRICE_TTL_HOURS,
                    TimeUnit.HOURS
            );
            
            // 2. SSE 전송
            StockPriceUpdateResponse response = StockPriceUpdateResponse.builder()
                    .stockCode(stockCode)
                    .currentPrice(event.getCurrentPrice())
                    .priceChange(event.getPriceChange())
                    .changeRate(event.getChangeRate())
                    .tradeTime(event.getTradeTime())
                    .volume(event.getVolume())
                    .build();
            
            sseEmitterManager.sendToStock(stockCode, response);
            
            // 3. 히스토리 버퍼에 추가
            StockPriceHistory history = StockPriceHistory.from(
                    stockCode,
                    event.getTradeTime(),
                    event.getCurrentPrice(),
                    event.getPriceChange(),
                    event.getChangeRate(),
                    event.getVolume()
            );
            
            buffer.add(history);
            
            log.debug("[EVENT] 가격 이벤트 처리: {} = {}원 (버퍼: {}개)", 
                    stockCode, event.getCurrentPrice(), buffer.size());
            log.trace("[EVENT] 히스토리 버퍼 추가 - 종목: {}, 가격: {}, 시간: {}, 버퍼 크기: {}", 
                    stockCode, event.getCurrentPrice(), event.getTradeTime(), buffer.size());
                    
        } catch (Exception e) {
            log.error("[EVENT] 가격 이벤트 처리 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 버퍼 플러시 - 5초마다 일괄 저장
     * - DB Insert 부하 최소화
     * - TransactionTemplate으로 명시적 트랜잭션 관리
     */
    @Scheduled(fixedDelay = 5000)
    @org.springframework.transaction.annotation.Transactional(
            transactionManager = "timescaleTransactionManager",
            propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW
    )
    public void flushBuffer() {
        int bufferSize = buffer.size();
        log.debug("🔄 [히스토리] flushBuffer 시작 - 버퍼 크기: {}", bufferSize);
        
        if (buffer.isEmpty()) {
            log.debug("🔄 [히스토리] 버퍼가 비어있음, 저장 건너뜀");
            return;
        }
        
        // 버퍼에서 꺼내기
        List<StockPriceHistory> batch = new ArrayList<>();
        StockPriceHistory history;
        while ((history = buffer.poll()) != null) {
            batch.add(history);
        }
        
        log.info("📦 [히스토리] 버퍼에서 {}건 추출 (버퍼 크기: {} → {})", 
                batch.size(), bufferSize, buffer.size());
        
        if (batch.isEmpty()) {
            return;
        }
        
        // 저장 전 총 개수 확인
        long countBefore = 0;
        try {
            countBefore = historyRepository.count();
            log.debug("📊 [히스토리] 저장 전 총 개수: {}", countBefore);
        } catch (Exception e) {
            log.error("❌ [히스토리] 저장 전 count() 실패: {}", e.getMessage());
        }
        
        // Repository의 saveAll() 사용 - @Transactional로 트랜잭션 자동 관리
        List<StockPriceHistory> saved = null;
        try {
            log.info("💾 [히스토리] TimescaleDB 저장 시작: {}건", batch.size());
            
            saved = historyRepository.saveAll(batch);
            
            log.info("✅ [히스토리] saveAll() 완료: {}건 처리", saved.size());
            
            // 저장된 엔티티 ID 확인
            if (!saved.isEmpty()) {
                log.debug("🆔 [히스토리] 저장된 첫 번째 ID: {}, 마지막 ID: {}", 
                        saved.get(0).getId(), saved.get(saved.size() - 1).getId());
            }
        } catch (Exception e) {
            log.error("❌ [히스토리] 저장 실패: {}", e.getMessage(), e);
            return;
        }
        
        if (saved == null || saved.isEmpty()) {
            log.error("❌ [히스토리] 저장 실패: saved가 null 또는 empty");
            return;
        }
        
        // 저장 후 즉시 조회하여 실제로 저장되었는지 확인
        long countAfter = 0;
        try {
            countAfter = historyRepository.count();
            log.debug("📊 [히스토리] 저장 후 총 개수: {}", countAfter);
        } catch (Exception e) {
            log.error("❌ [히스토리] 저장 후 count() 실패: {}", e.getMessage());
        }
        long actualSaved = countAfter - countBefore;
        
        log.info("💾 [히스토리] {}건 저장 완료 (첫 번째: {}, 마지막: {}, 저장 전: {}건, 저장 후: {}건, 실제 저장: {}건)", 
                saved.size(),
                saved.get(0).getStockCode(),
                saved.get(saved.size() - 1).getStockCode(),
                countBefore,
                countAfter,
                actualSaved);
        
        // 실제 저장이 안 된 경우 경고
        if (actualSaved == 0 && saved.size() > 0) {
            log.error("❌ [히스토리] ⚠️ 경고: saveAll()은 성공했지만 실제 DB에는 저장되지 않음!");
            log.error("❌ [히스토리] TimescaleDB 연결 상태를 확인하세요!");
            log.error("❌ [히스토리] URL: jdbc:postgresql://localhost:5432/payflow_timeseries");
        }
    }
}
