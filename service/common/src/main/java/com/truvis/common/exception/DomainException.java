package com.truvis.common.exception;

public class DomainException extends BusinessException {
    
    public DomainException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public DomainException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
