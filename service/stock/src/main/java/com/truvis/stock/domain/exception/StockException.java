package com.truvis.stock.domain.exception;

import com.truvis.common.exception.DomainException;

/**
 * Stock 도메인 예외 기본 클래스
 * - 모든 Stock 관련 예외의 부모 클래스
 */
public class StockException extends DomainException {
    
    public StockException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public StockException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
