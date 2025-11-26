package com.truvis.stock.domain.exception;

import com.truvis.common.model.vo.StockCode;

/**
 * 종목을 찾을 수 없을 때 발생하는 예외
 * - HTTP 404 Not Found에 매핑
 */
public class StockNotFoundException extends StockException {
    
    private static final String ERROR_CODE = "STOCK_NOT_FOUND";
    
    public StockNotFoundException(String stockCode) {
        super(ERROR_CODE, String.format("종목을 찾을 수 없습니다: %s", stockCode));
    }
    
    public StockNotFoundException(StockCode stockCode) {
        super(ERROR_CODE, String.format("종목을 찾을 수 없습니다: %s", stockCode.getValue()));
    }
    
    public StockNotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
