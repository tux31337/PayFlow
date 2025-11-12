package com.truvis.stock.config;

import com.truvis.common.model.vo.StockCode;
import com.truvis.stock.application.StockApplicationService;
import com.truvis.stock.domain.CurrentPrice;
import com.truvis.stock.domain.Market;
import com.truvis.stock.domain.Stock;
import com.truvis.stock.infrastructure.KisApiStockPriceProvider;
import com.truvis.stock.infrastructure.websocket.KisWebSocketClient;
import com.truvis.stock.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Stock 초기 데이터 로딩
 * 
 * 1. 주요 종목 등록
 * 2. REST API로 초기 가격 조회
 * 3. WebSocket 구독 시작
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class StockDataInitializer {

    private final StockApplicationService stockApplicationService;
    private final StockRepository stockRepository;
    private final KisApiStockPriceProvider priceProvider;
    private final KisWebSocketClient webSocketClient;

    @PostConstruct
    public void initialize() {
        log.info("========================================");
        log.info("📊 [초기화] 종목 데이터 로딩 시작");
        log.info("========================================");

        // 1. 주요 종목 등록
        List<Stock> stocks = initializeStocks();
        
        // 2. 초기 가격 조회 (REST)
        updateInitialPrices(stocks);
        
        // 3. WebSocket 구독 (실시간)
        subscribeToWebSocket(stocks);

        log.info("========================================");
        log.info("✅ [초기화] 완료: {}개 종목 준비됨", stocks.size());
        log.info("========================================");
    }

    /**
     * 주요 종목 등록
     */
    private List<Stock> initializeStocks() {
        log.info("📝 [1/3] 종목 등록 중...");
        
        List<StockInfo> stockInfos = List.of(
            // 대형주 - 반도체
            new StockInfo("005930", "삼성전자", Market.KOSPI, "반도체"),
            new StockInfo("000660", "SK하이닉스", Market.KOSPI, "반도체"),
            
            // IT/인터넷
            new StockInfo("035420", "NAVER", Market.KOSPI, "인터넷"),
            new StockInfo("035720", "카카오", Market.KOSPI, "인터넷"),
            new StockInfo("036570", "엔씨소프트", Market.KOSDAQ, "게임"),
            
            // 자동차
            new StockInfo("005380", "현대차", Market.KOSPI, "자동차"),
            new StockInfo("000270", "기아", Market.KOSPI, "자동차"),
            
            // 금융
            new StockInfo("055550", "신한지주", Market.KOSPI, "금융"),
            new StockInfo("105560", "KB금융", Market.KOSPI, "금융"),
            
            // 바이오/화학
            new StockInfo("068270", "셀트리온", Market.KOSPI, "바이오"),
            new StockInfo("207940", "삼성바이오로직스", Market.KOSPI, "바이오"),
            new StockInfo("051910", "LG화학", Market.KOSPI, "화학"),
            
            // 유통/식품
            new StockInfo("028260", "삼성물산", Market.KOSPI, "유통"),
            new StockInfo("097950", "CJ제일제당", Market.KOSPI, "식품")
        );

        List<Stock> stocks = new ArrayList<>();
        
        for (StockInfo info : stockInfos) {
            try {
                if (!stockApplicationService.existsStock(info.code)) {
                    stockApplicationService.registerStock(
                        info.code, 
                        info.name, 
                        info.market, 
                        info.sector
                    );
                    log.info("  ✓ {} ({}) 등록 완료", info.name, info.code);
                } else {
                    log.debug("  → {} ({}) 이미 존재", info.name, info.code);
                }
                
                // 등록된 종목 조회
                Stock stock = stockRepository.findByStockCode(StockCode.of(info.code))
                    .orElseThrow();
                stocks.add(stock);
                
                // Rate Limit 방지
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("  ✗ {} ({}) 등록 실패: {}", info.name, info.code, e.getMessage());
            }
        }
        
        log.info("  → 총 {}개 종목 등록됨", stocks.size());
        return stocks;
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

    /**
     * 종목 정보 DTO
     */
    private record StockInfo(
        String code,
        String name,
        Market market,
        String sector
    ) {}
}
