package com.truvis.common.exception;

import com.truvis.common.model.vo.StockCode;

/**
 * 📈 Stock 도메인 예외
 * 
 * ErrorCode Enum 기반으로 HttpStatus가 자동으로 결정됩니다.
 */
public class StockException extends BusinessException {

    public StockException(StockErrorCode errorCode) {
        super(errorCode);
    }
    
    public StockException(StockErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public StockException(StockErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public StockException(StockErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode, customMessage, cause);
    }

    // ==================== 종목 조회 관련 (404) ====================

    /**
     * 종목을 찾을 수 없음
     */
    public static StockException notFound(String stockCode) {
        return new StockException(
                StockErrorCode.NOT_FOUND,
                String.format("종목을 찾을 수 없습니다: %s", stockCode)
        );
    }

    public static StockException notFound(StockCode stockCode) {
        return notFound(stockCode.getValue());
    }

    // ==================== 종목 등록 관련 (409) ====================

    /**
     * 종목이 이미 존재함
     */
    public static StockException alreadyExists(String stockCode) {
        return new StockException(
                StockErrorCode.ALREADY_EXISTS,
                String.format("이미 등록된 종목입니다: %s", stockCode)
        );
    }

    public static StockException alreadyExists(StockCode stockCode) {
        return alreadyExists(stockCode.getValue());
    }

    // ==================== 잘못된 데이터 관련 (400) ====================

    /**
     * 잘못된 종목 데이터
     */
    public static StockException invalidData(String message) {
        return new StockException(StockErrorCode.INVALID_DATA, message);
    }

    /**
     * 잘못된 종목 코드 형식
     */
    public static StockException invalidStockCode(String stockCode) {
        return new StockException(
                StockErrorCode.INVALID_DATA,
                String.format("잘못된 종목 코드 형식: %s", stockCode)
        );
    }

    /**
     * 잘못된 가격
     */
    public static StockException invalidPrice(String stockCode, String price) {
        return new StockException(
                StockErrorCode.INVALID_PRICE,
                String.format("종목 %s의 가격이 유효하지 않습니다: %s", stockCode, price)
        );
    }

    // ==================== 주가 제공자 관련 (503) ====================

    /**
     * API 호출 실패
     */
    public static StockException priceProviderApiCallFailed(String stockCode, Throwable cause) {
        return new StockException(
                StockErrorCode.PROVIDER_API_CALL_FAILED,
                String.format("종목 %s의 가격 조회 실패: API 호출 오류", stockCode),
                cause
        );
    }

    /**
     * 네트워크 오류
     */
    public static StockException priceProviderNetworkError(String stockCode, Throwable cause) {
        return new StockException(
                StockErrorCode.PROVIDER_NETWORK_ERROR,
                String.format("종목 %s의 가격 조회 실패: 네트워크 오류", stockCode),
                cause
        );
    }

    /**
     * Rate Limit 초과
     */
    public static StockException priceProviderRateLimitExceeded(String stockCode) {
        return new StockException(
                StockErrorCode.PROVIDER_RATE_LIMIT,
                String.format("종목 %s의 가격 조회 실패: API 호출 한도 초과", stockCode)
        );
    }

    /**
     * 타임아웃
     */
    public static StockException priceProviderTimeout(String stockCode) {
        return new StockException(
                StockErrorCode.PROVIDER_TIMEOUT,
                String.format("종목 %s의 가격 조회 실패: 타임아웃", stockCode)
        );
    }

    /**
     * 일반적인 Provider 오류
     */
    public static StockException priceProviderError(String message) {
        return new StockException(StockErrorCode.PROVIDER_ERROR, message);
    }

    public static StockException priceProviderError(String message, Throwable cause) {
        return new StockException(StockErrorCode.PROVIDER_ERROR, message, cause);
    }
}
