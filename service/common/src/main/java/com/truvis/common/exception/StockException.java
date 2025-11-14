package com.truvis.common.exception;

import com.truvis.common.model.vo.StockCode;

/**
 * Stock 도메인 예외
 * - 정적 팩토리 메서드 패턴 사용
 */
public class StockException extends BusinessException {

    public StockException(String errorCode, String message) {
        super(errorCode, message);
    }

    public StockException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    // ==================== 종목 조회 관련 ====================

    /**
     * 종목을 찾을 수 없음 (404)
     */
    public static StockException notFound(String stockCode) {
        return new StockException("STOCK_001", 
            String.format("종목을 찾을 수 없습니다: %s", stockCode));
    }

    public static StockException notFound(StockCode stockCode) {
        return notFound(stockCode.getValue());
    }

    // ==================== 종목 등록 관련 ====================

    /**
     * 종목이 이미 존재함 (409)
     */
    public static StockException alreadyExists(String stockCode) {
        return new StockException("STOCK_002", 
            String.format("이미 등록된 종목입니다: %s", stockCode));
    }

    public static StockException alreadyExists(StockCode stockCode) {
        return alreadyExists(stockCode.getValue());
    }

    /**
     * 잘못된 종목 데이터 (400)
     */
    public static StockException invalidData(String message) {
        return new StockException("STOCK_003", message);
    }

    /**
     * 잘못된 종목 코드 형식 (400)
     */
    public static StockException invalidStockCode(String stockCode) {
        return new StockException("STOCK_003", 
            String.format("잘못된 종목 코드 형식: %s", stockCode));
    }

    /**
     * 잘못된 가격 (400)
     */
    public static StockException invalidPrice(String stockCode, String price) {
        return new StockException("STOCK_004", 
            String.format("종목 %s의 가격이 유효하지 않습니다: %s", stockCode, price));
    }

    // ==================== 주가 제공자 관련 (503) ====================

    /**
     * API 호출 실패
     */
    public static StockException priceProviderApiCallFailed(String stockCode, Throwable cause) {
        return new StockException("STOCK_005", 
            String.format("종목 %s의 가격 조회 실패: API 호출 오류", stockCode), 
            cause);
    }

    /**
     * 네트워크 오류
     */
    public static StockException priceProviderNetworkError(String stockCode, Throwable cause) {
        return new StockException("STOCK_006", 
            String.format("종목 %s의 가격 조회 실패: 네트워크 오류", stockCode), 
            cause);
    }

    /**
     * Rate Limit 초과
     */
    public static StockException priceProviderRateLimitExceeded(String stockCode) {
        return new StockException("STOCK_007", 
            String.format("종목 %s의 가격 조회 실패: API 호출 한도 초과", stockCode));
    }

    /**
     * 타임아웃
     */
    public static StockException priceProviderTimeout(String stockCode) {
        return new StockException("STOCK_008", 
            String.format("종목 %s의 가격 조회 실패: 타임아웃", stockCode));
    }

    /**
     * 일반적인 Provider 오류
     */
    public static StockException priceProviderError(String message) {
        return new StockException("STOCK_009", message);
    }

    public static StockException priceProviderError(String message, Throwable cause) {
        return new StockException("STOCK_009", message, cause);
    }
}
