package com.truvis.stock.domain.exception;

import com.truvis.common.model.vo.StockCode;

/**
 * 종목이 이미 존재할 때 발생하는 예외
 * - HTTP 409 Conflict에 매핑
 */
public class StockAlreadyExistsException extends StockException {
    
    private static final String ERROR_CODE = "STOCK_ALREADY_EXISTS";
    
    public StockAlreadyExistsException(String stockCode) {
        super(ERROR_CODE, String.format("이미 등록된 종목입니다: %s", stockCode));
    }
    
    public StockAlreadyExistsException(StockCode stockCode) {
        super(ERROR_CODE, String.format("이미 등록된 종목입니다: %s", stockCode.getValue()));
    }
}
