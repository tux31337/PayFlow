package com.truvis.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 🎯 에러 코드 인터페이스
 * 
 * 모든 도메인별 에러 코드 Enum이 구현해야 하는 인터페이스.
 * 각 에러 코드가 자신의 HttpStatus를 알고 있어서 GlobalExceptionHandler의 if-else를 없앨 수 있어요!
 */
public interface ErrorCode {
    
    /**
     * 에러 코드 (예: "ORDER_001", "STOCK_002")
     */
    String getCode();
    
    /**
     * 에러 메시지 템플릿
     */
    String getMessage();
    
    /**
     * HTTP 상태 코드
     */
    HttpStatus getHttpStatus();
    
    /**
     * 로그 레벨 (기본: WARN)
     */
    default LogLevel getLogLevel() {
        return LogLevel.WARN;
    }
    
    enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}

