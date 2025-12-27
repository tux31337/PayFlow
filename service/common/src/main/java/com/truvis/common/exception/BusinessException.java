package com.truvis.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 🎯 비즈니스 예외 기본 클래스
 * 
 * ErrorCode를 기반으로 예외를 생성하면 HttpStatus, 로그 레벨 등을 
 * GlobalExceptionHandler에서 if-else 없이 자동으로 처리할 수 있어요!
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final ErrorCode.LogLevel logLevel;
    
    /**
     * ErrorCode Enum 기반 생성자 (권장)
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.logLevel = errorCode.getLogLevel();
    }
    
    /**
     * ErrorCode Enum + 커스텀 메시지 생성자
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.logLevel = errorCode.getLogLevel();
    }
    
    /**
     * ErrorCode Enum + 원인 예외 생성자
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.logLevel = errorCode.getLogLevel();
    }
    
    /**
     * ErrorCode Enum + 커스텀 메시지 + 원인 예외 생성자
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.logLevel = errorCode.getLogLevel();
    }
    
    /**
     * 에러 코드 문자열 반환 (예: "ORDER_001")
     */
    public String getErrorCodeValue() {
        return errorCode.getCode();
    }
    
    // ========== 기존 호환성을 위한 생성자 (Deprecated, 점진적 마이그레이션용) ==========
    
    @Deprecated
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = new LegacyErrorCode(errorCode, message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.logLevel = ErrorCode.LogLevel.WARN;
    }
    
    @Deprecated
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = new LegacyErrorCode(errorCode, message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.logLevel = ErrorCode.LogLevel.WARN;
    }
    
    /**
     * 기존 문자열 에러 코드 호환을 위한 레거시 래퍼
     */
    @Deprecated
    private record LegacyErrorCode(String code, String message) implements ErrorCode {
        @Override
        public String getCode() { return code; }
        
        @Override
        public String getMessage() { return message; }
        
        @Override
        public HttpStatus getHttpStatus() { return HttpStatus.BAD_REQUEST; }
    }
}
