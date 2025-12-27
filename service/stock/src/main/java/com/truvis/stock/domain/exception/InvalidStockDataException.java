package com.truvis.stock.domain.exception;

/**
 * 잘못된 종목 데이터 예외
 * - 유효성 검증 실패 시 발생
 * - HTTP 400 Bad Request에 매핑
 */
public class InvalidStockDataException extends StockException {
    
    private static final String ERROR_CODE = "INVALID_STOCK_DATA";
    
    public InvalidStockDataException(String message) {
        super(ERROR_CODE, message);
    }
    
    public InvalidStockDataException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
    
    /**
     * 잘못된 종목 코드 형식
     */
    public static InvalidStockDataException invalidStockCode(String stockCode) {
        return new InvalidStockDataException(
                String.format("잘못된 종목 코드 형식: %s", stockCode)
        );
    }
    
    /**
     * 잘못된 가격
     */
    public static InvalidStockDataException invalidPrice(String stockCode, String price) {
        return new InvalidStockDataException(
                String.format("종목 %s의 가격이 유효하지 않습니다: %s", stockCode, price)
        );
    }
}
