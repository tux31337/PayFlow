package com.truvis.stock.infrastructure.exception;

import com.truvis.common.exception.BusinessException;

/**
 * Stock Price Provider 관련 예외
 * - API 호출 실패, 네트워크 오류 등
 * - HTTP 503 Service Unavailable에 매핑
 */
public class StockPriceProviderException extends BusinessException {
    
    private static final String ERROR_CODE = "STOCK_PRICE_PROVIDER_ERROR";
    
    public StockPriceProviderException(String message) {
        super(ERROR_CODE, message);
    }
    
    public StockPriceProviderException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
    
    /**
     * API 호출 실패
     */
    public static StockPriceProviderException apiCallFailed(String stockCode, Throwable cause) {
        return new StockPriceProviderException(
                String.format("종목 %s의 가격 조회 실패: API 호출 오류", stockCode),
                cause
        );
    }
    
    /**
     * 네트워크 오류
     */
    public static StockPriceProviderException networkError(String stockCode, Throwable cause) {
        return new StockPriceProviderException(
                String.format("종목 %s의 가격 조회 실패: 네트워크 오류", stockCode),
                cause
        );
    }
    
    /**
     * Rate Limit 초과
     */
    public static StockPriceProviderException rateLimitExceeded(String stockCode) {
        return new StockPriceProviderException(
                String.format("종목 %s의 가격 조회 실패: API 호출 한도 초과", stockCode)
        );
    }
    
    /**
     * 타임아웃
     */
    public static StockPriceProviderException timeout(String stockCode) {
        return new StockPriceProviderException(
                String.format("종목 %s의 가격 조회 실패: 타임아웃", stockCode)
        );
    }
}
