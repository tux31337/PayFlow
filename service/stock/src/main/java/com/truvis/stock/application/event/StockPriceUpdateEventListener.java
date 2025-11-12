package com.truvis.stock.application.event;

import com.truvis.stock.domain.StockPriceHistory;
import com.truvis.stock.domain.event.StockPriceUpdateEvent;
import com.truvis.stock.infrastructure.sse.SseEmitterManager;
import com.truvis.stock.model.StockPriceUpdateResponse;
import com.truvis.stock.repository.StockPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
@RequiredArgsConstructor
public class StockPriceUpdateEventListener {
    
    private final SseEmitterManager sseEmitterManager;
    private final StockPriceHistoryRepository historyRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
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
                    
        } catch (Exception e) {
            log.error("[EVENT] 가격 이벤트 처리 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 버퍼 플러시 - 5초마다 일괄 저장
     * - DB Insert 부하 최소화
     */
    @Scheduled(fixedDelay = 5000)
    public void flushBuffer() {
        if (buffer.isEmpty()) {
            return;
        }
        
        try {
            // 버퍼에서 꺼내기
            List<StockPriceHistory> batch = new ArrayList<>();
            StockPriceHistory history;
            while ((history = buffer.poll()) != null) {
                batch.add(history);
            }
            
            if (!batch.isEmpty()) {
                // 일괄 저장
                historyRepository.saveAll(batch);
                log.info("💾 [히스토리] {}건 저장 완료", batch.size());
            }
            
        } catch (Exception e) {
            log.error("❌ [히스토리] 저장 실패: {}", e.getMessage(), e);
        }
    }
}
