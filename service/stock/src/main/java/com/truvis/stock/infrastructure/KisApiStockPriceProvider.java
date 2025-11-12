package com.truvis.stock.infrastructure;

import com.truvis.common.model.vo.StockCode;
import com.truvis.stock.domain.CurrentPrice;
import com.truvis.stock.domain.StockPriceProvider;
import com.truvis.stock.infrastructure.kis.KisApiResponse;
import com.truvis.stock.infrastructure.kis.KisTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 한국투자증권 OpenAPI 주식 가격 제공자
 * - 실제 API 호출하여 실시간 주가 조회
 * - 운영 환경에서 사용
 *
 * API 문서: https://apiportal.koreainvestment.com
 *
 * 활성화 조건:
 * - spring.profiles.active=prod
 */
@Slf4j
@Component
//@Profile("prod")  // 운영 환경에서만 활성화
@Profile({"local", "dev", "test"})  // 로컬/개발/테스트 환경에서만 활성화
public class KisApiStockPriceProvider implements StockPriceProvider {

    private static final String BASE_URL = "https://openapi.koreainvestment.com:9443";
    private static final String TOKEN_URL = BASE_URL + "/oauth2/tokenP";
    private static final String PRICE_URL = BASE_URL + "/uapi/domestic-stock/v1/quotations/inquire-price";

    @Value("${kis.api.app-key:}")
    private String appKey;

    @Value("${kis.api.app-secret:}")
    private String appSecret;

    private final RestTemplate restTemplate;

    /**
     * 현재 사용 중인 Access Token
     * - 24시간 유효
     * - 만료되면 자동 재발급
     */
    private KisTokenResponse currentToken;
    
    /**
     * 마지막 API 호출 시각
     * - Rate Limit 방지용
     */
    private long lastApiCallTime = 0;
    
    /**
     * API 호출 최소 간격 (밀리초)
     * - KIS API: 초당 최대 5회 → 200ms 간격 필요
     * - 안전을 위해 500ms로 설정 (여유있게!)
     */
    private static final long MIN_API_CALL_INTERVAL = 500;

    public KisApiStockPriceProvider() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public CurrentPrice getCurrentPrice(StockCode stockCode) {
        String code = stockCode.getValue();

        log.info("[KIS_API] 종목 {} 현재가 조회 시작", code);

        try {
            // 1. 토큰 확인 및 갱신
            ensureValidToken();

            // 2. Rate Limit 방지 (초당 5회 제한)
            rateLimitDelay();

            // 3. API 호출
            KisApiResponse response = callPriceApi(code);

            // 4. 응답 검증
            if (!"0".equals(response.getRtCd())) {
                throw new RuntimeException("API 오류: " + response.getMsg1());
            }

            // 5. 가격 파싱
            String priceStr = response.getOutput().getStckPrpr();
            long price = Long.parseLong(priceStr);

            log.info("[KIS_API] 종목 {} 현재가: {}", code, price);
            return CurrentPrice.of(price);

        } catch (RestClientException e) {
            log.error("[KIS_API] 네트워크 오류 발생: {}", e.getMessage());
            throw new RuntimeException("주가 조회 실패: 네트워크 오류", e);
        } catch (Exception e) {
            log.error("[KIS_API] 예상치 못한 오류: {}", e.getMessage(), e);
            throw new RuntimeException("주가 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<StockCode, CurrentPrice> getCurrentPrices(List<StockCode> stockCodes) {
        log.info("[KIS_API] 일괄 가격 조회: {} 종목", stockCodes.size());

        Map<StockCode, CurrentPrice> result = new HashMap<>();

        // TODO: 실제로는 일괄 조회 API 사용 (성능 최적화)
        // 지금은 하나씩 호출
        for (StockCode stockCode : stockCodes) {
            try {
                CurrentPrice price = getCurrentPrice(stockCode);
                result.put(stockCode, price);
            } catch (Exception e) {
                log.warn("[KIS_API] 종목 {} 조회 실패, 건너뜀: {}",
                        stockCode.getValue(), e.getMessage());
                // 실패한 종목은 결과에서 제외
            }
        }

        log.info("[KIS_API] 일괄 조회 완료: {}/{} 성공", result.size(), stockCodes.size());
        return result;
    }

    @Override
    public String getProviderType() {
        return "KIS_API";
    }

    @Override
    public boolean isHealthy() {
        try {
            // 토큰 발급 테스트
            ensureValidToken();
            return currentToken != null && !currentToken.isExpired();
        } catch (Exception e) {
            log.error("[KIS_API] Health Check 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Access Token 유효성 확인 및 갱신
     */
    private void ensureValidToken() {
        if (currentToken == null || currentToken.isExpired()) {
            log.info("[KIS_API] 토큰 발급 필요");
            currentToken = issueToken();
            log.info("[KIS_API] 토큰 발급 완료");
        }
    }
    
    /**
     * Rate Limit 방지를 위한 대기
     * - KIS API: 초당 최대 5회 호출
     * - 마지막 호출 후 250ms 대기
     */
    private void rateLimitDelay() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCall = currentTime - lastApiCallTime;
        
        if (timeSinceLastCall < MIN_API_CALL_INTERVAL) {
            long waitTime = MIN_API_CALL_INTERVAL - timeSinceLastCall;
            try {
                log.debug("[KIS_API] Rate Limit 대기: {}ms", waitTime);
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        lastApiCallTime = System.currentTimeMillis();
    }

    /**
     * Access Token 발급
     */
    private KisTokenResponse issueToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("appsecret", appSecret);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<KisTokenResponse> response = restTemplate.postForEntity(
                    TOKEN_URL,
                    request,
                    KisTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("토큰 발급 실패: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("[KIS_API] 토큰 발급 실패: {}", e.getMessage());
            throw new RuntimeException("토큰 발급 실패", e);
        }
    }

    /**
     * 현재가 조회 API 호출
     */
    private KisApiResponse callPriceApi(String stockCode) {
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + currentToken.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "FHKST01010100");  // 거래 ID (현재가 조회)
        headers.setContentType(MediaType.APPLICATION_JSON);

        // URL 파라미터
        String url = PRICE_URL +
                "?FID_COND_MRKT_DIV_CODE=J" +  // 시장 구분 (J: 주식)
                "&FID_INPUT_ISCD=" + stockCode;  // 종목 코드

        HttpEntity<Void> request = new HttpEntity<>(headers);

        // API 호출
        ResponseEntity<KisApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                KisApiResponse.class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("API 호출 실패: " + response.getStatusCode());
        }

        return response.getBody();
    }
}