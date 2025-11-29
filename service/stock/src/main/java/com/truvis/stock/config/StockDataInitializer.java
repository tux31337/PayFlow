package com.truvis.stock.config;

import com.truvis.stock.domain.CurrentPrice;
import com.truvis.stock.domain.Stock;
import com.truvis.stock.infrastructure.JpaStockRepository;
import com.truvis.stock.infrastructure.KisApiStockPriceProvider;
import com.truvis.stock.infrastructure.websocket.KisWebSocketClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stock 초기 데이터 로딩
 * 
 * 1. Flyway로 종목 데이터는 이미 등록됨 (가격 0원)
 * 2. REST API로 초기 가격 조회 및 업데이트
 * 3. WebSocket 구독 시작
 */
@Slf4j
@Component
@Profile({"local", "prod"})  // 로컬 및 프로덕션에서 실행
@RequiredArgsConstructor
public class StockDataInitializer {

    private final JpaStockRepository stockRepository;
    private final KisApiStockPriceProvider priceProvider;
    private final KisWebSocketClient webSocketClient;

    @PostConstruct
    public void initialize() {
        log.info("========================================");
        log.info("📊 [초기화] 종목 가격 업데이트 시작");
        log.info("========================================");

        // 1. DB에서 모든 종목 조회 (Flyway로 이미 등록됨)
        List<Stock> stocks = stockRepository.findAll();
        
        if (stocks.isEmpty()) {
            log.warn("⚠️  [초기화] 등록된 종목이 없습니다. Flyway 마이그레이션을 확인하세요.");
            return;
        }
        
        log.info("📝 [1/3] {}개 종목 조회 완료 (Flyway 마이그레이션)", stocks.size());
        
        // 2. 초기 가격 조회 및 업데이트 (REST API)
        updateInitialPrices(stocks);
        
        // 3. WebSocket 구독 (실시간)
        subscribeToWebSocket(stocks);

        log.info("========================================");
        log.info("✅ [초기화] 완료: {}개 종목 준비됨", stocks.size());
        log.info("========================================");
    }

    /**
     * 초기 가격 조회 (REST API)
     */
    private void updateInitialPrices(List<Stock> stocks) {
        log.info("💰 [2/3] 초기 가격 조회 중... (REST API)");
        
        int successCount = 0;
        
        for (Stock stock : stocks) {
            try {
                CurrentPrice price = priceProvider.getCurrentPrice(stock.getStockCode());
                stock.updatePrice(price);
                stockRepository.save(stock);
                
                log.info("  ✓ {} = {:,}원", 
                    stock.getName().getValue(), 
                    price.getValue());
                
                successCount++;
                
                // Rate Limit 방지 (500ms)
                Thread.sleep(500);
                
            } catch (Exception e) {
                log.warn("  ✗ {} 가격 조회 실패: {}", 
                    stock.getName().getValue(), 
                    e.getMessage());
            }
        }
        
        log.info("  → {}/{}개 종목 가격 조회 완료", successCount, stocks.size());
    }

    /**
     * WebSocket 구독 시작
     */
    private void subscribeToWebSocket(List<Stock> stocks) {
        log.info("🔌 [3/3] WebSocket 실시간 구독 시작...");
        
        // WebSocket이 아직 연결 안됐으면 대기
        int retryCount = 0;
        while (!webSocketClient.isConnected() && retryCount < 10) {
            try {
                log.debug("  ⏳ WebSocket 연결 대기 중... ({}/10)", retryCount + 1);
                Thread.sleep(1000);
                retryCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (!webSocketClient.isConnected()) {
            log.warn("  ⚠️  WebSocket 연결 실패, 구독 건너뜀");
            return;
        }
        
        // 구독 시작
        for (Stock stock : stocks) {
            try {
                webSocketClient.subscribe(stock.getStockCode());
                log.info("  ✓ {} 구독 완료", stock.getName().getValue());
                
                // 구독 간격 (안정성)
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("  ✗ {} 구독 실패: {}", 
                    stock.getName().getValue(), 
                    e.getMessage());
            }
        }
        
        log.info("  → WebSocket 구독 완료");
    }

}
