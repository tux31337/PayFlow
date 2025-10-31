package com.truvis.stock.infrastructure;

import com.truvis.common.model.vo.StockCode;
import com.truvis.stock.domain.CurrentPrice;
import com.truvis.stock.domain.StockPriceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock 주식 가격 제공자
 * - 개발/테스트 환경에서 사용
 * - 실제 API 호출 없이 고정된 Mock 데이터 반환
 * - 외부 API 비용 절감, 빠른 개발 가능
 *
 * 활성화 조건:
 * - spring.profiles.active=dev
 * - spring.profiles.active=test
 */
@Slf4j
@Component
@Profile({"dev", "test"})  // 개발/테스트 환경에서만 활성화
public class MockStockPriceProvider implements StockPriceProvider {

    /**
     * 미리 정의된 Mock 가격 데이터
     * - 한국 주요 종목
     * - 미국 주요 종목
     */
    private static final Map<String, Long> MOCK_PRICES = new HashMap<>();

    static {
        // ========== 한국 종목 (원화) ==========
        MOCK_PRICES.put("005930", 71000L);   // 삼성전자
        MOCK_PRICES.put("000660", 145000L);  // SK하이닉스
        MOCK_PRICES.put("035420", 210000L);  // NAVER
        MOCK_PRICES.put("005380", 55000L);   // 현대차
        MOCK_PRICES.put("051910", 38000L);   // LG화학
        MOCK_PRICES.put("006400", 42000L);   // 삼성SDI
        MOCK_PRICES.put("035720", 95000L);   // 카카오
        MOCK_PRICES.put("207940", 82000L);   // 삼성바이오로직스
        MOCK_PRICES.put("068270", 125000L);  // 셀트리온
        MOCK_PRICES.put("028260", 280000L);  // 삼성물산

        // ========== 미국 종목 (달러 * 100) ==========
        // 실제 $180.50 → 18050 (센트 단위로 저장)
        MOCK_PRICES.put("AAPL", 18050L);    // Apple
        MOCK_PRICES.put("MSFT", 37520L);    // Microsoft
        MOCK_PRICES.put("GOOGL", 14150L);   // Alphabet (Google)
        MOCK_PRICES.put("AMZN", 17880L);    // Amazon
        MOCK_PRICES.put("TSLA", 24560L);    // Tesla
        MOCK_PRICES.put("META", 48220L);    // Meta (Facebook)
        MOCK_PRICES.put("NVDA", 48150L);    // NVIDIA
        MOCK_PRICES.put("BRK.B", 38950L);   // Berkshire Hathaway
        MOCK_PRICES.put("JPM", 19870L);     // JPMorgan Chase
        MOCK_PRICES.put("V", 27940L);       // Visa
    }

    /**
     * 랜덤 가격 생성 범위
     * - 한국: 10,000원 ~ 100,000원
     * - 미국: $100 ~ $500 (10000 ~ 50000 센트)
     */
    private static final long MIN_PRICE_KR = 10000L;
    private static final long MAX_PRICE_KR = 100000L;
    private static final long MIN_PRICE_US = 10000L;  // $100
    private static final long MAX_PRICE_US = 50000L;  // $500

    @Override
    public CurrentPrice getCurrentPrice(StockCode stockCode) {
        String code = stockCode.getValue();

        log.debug("MockStockPriceProvider.getCurrentPrice() 호출: {}", code);

        // Mock 데이터에 있으면 반환
        if (MOCK_PRICES.containsKey(code)) {
            long price = MOCK_PRICES.get(code);
            log.info("[MOCK] 종목 {} 가격 조회: {} (사전 정의된 값)", code, price);
            return CurrentPrice.of(price);
        }

        // Mock 데이터에 없으면 랜덤 생성
        long randomPrice = generateRandomPrice(code);
        log.info("[MOCK] 종목 {} 가격 조회: {} (랜덤 생성)", code, randomPrice);
        return CurrentPrice.of(randomPrice);
    }

    @Override
    public Map<StockCode, CurrentPrice> getCurrentPrices(List<StockCode> stockCodes) {
        log.info("[MOCK] 일괄 가격 조회: {} 종목", stockCodes.size());

        Map<StockCode, CurrentPrice> result = new HashMap<>();

        for (StockCode stockCode : stockCodes) {
            CurrentPrice price = getCurrentPrice(stockCode);
            result.put(stockCode, price);
        }

        log.debug("[MOCK] 일괄 조회 완료: {} 종목", result.size());
        return result;
    }

    @Override
    public String getProviderType() {
        return "MOCK";
    }

    @Override
    public boolean isHealthy() {
        // Mock은 항상 정상
        return true;
    }

    /**
     * 랜덤 가격 생성
     * - 한국 종목 (6자리 숫자): 10,000 ~ 100,000원
     * - 미국 종목 (영문): $100 ~ $500
     */
    private long generateRandomPrice(String code) {
        // 한국 종목 판단 (숫자로만 구성)
        boolean isKorean = code.matches("\\d+");

        if (isKorean) {
            return ThreadLocalRandom.current().nextLong(MIN_PRICE_KR, MAX_PRICE_KR);
        } else {
            // 미국 종목
            return ThreadLocalRandom.current().nextLong(MIN_PRICE_US, MAX_PRICE_US);
        }
    }

    /**
     * Mock 데이터에 종목 추가 (테스트용)
     */
    public void addMockPrice(String code, long price) {
        MOCK_PRICES.put(code, price);
        log.info("[MOCK] 종목 {} Mock 데이터 추가: {}", code, price);
    }

    /**
     * 현재 Mock 데이터 개수 조회 (테스트/디버깅용)
     */
    public int getMockDataCount() {
        return MOCK_PRICES.size();
    }
}