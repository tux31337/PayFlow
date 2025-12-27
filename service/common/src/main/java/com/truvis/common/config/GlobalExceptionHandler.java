package com.truvis.common.config;

import com.truvis.common.exception.BusinessException;
import com.truvis.common.exception.ErrorCode;
import com.truvis.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 🌍 글로벌 예외 처리기
 * 
 * - BusinessException 하위 클래스: 모두 통합 핸들러에서 처리
 *   (OrderException, StockException, PortfolioException, EmailVerificationException 등)
 */
@RestControllerAdvice  
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 🎯 BusinessException 통합 핸들러
     * 
     * 모든 도메인 예외가 이 핸들러 하나로 처리됩니다!
     * HttpStatus, 로그 레벨 모두 ErrorCode Enum에서 자동 결정.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        logByLevel(e.getLogLevel(), e);
        
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ErrorResponse.of(e.getMessage(), e.getErrorCodeValue()));
    }
    
    private void logByLevel(ErrorCode.LogLevel level, BusinessException e) {
        String message = "[{}] {}: {}";
        String errorCode = e.getErrorCodeValue();
        String errorMessage = e.getMessage();
        
        switch (level) {
            case DEBUG -> log.debug(message, errorCode, e.getClass().getSimpleName(), errorMessage);
            case INFO -> log.info(message, errorCode, e.getClass().getSimpleName(), errorMessage);
            case WARN -> log.warn(message, errorCode, e.getClass().getSimpleName(), errorMessage);
            case ERROR -> log.error(message, errorCode, e.getClass().getSimpleName(), errorMessage, e);
        }
    }
    
    /**
     * 📝 Validation 예외 처리 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        
        String message = bindingResult.getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        log.warn("[VALIDATION_ERROR] {}", message);
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of(message, "VALIDATION_ERROR"));
    }
    
    /**
     * ⚠️ IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("[BAD_REQUEST] IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ErrorResponse.badRequest(e.getMessage()));
    }

    /**
     * 🔒 IllegalStateException 처리 (인증 관련 시 401)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        if (e.getMessage() != null && e.getMessage().contains("인증")) {
            log.warn("[AUTH_REQUIRED] {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(e.getMessage(), "AUTH_REQUIRED"));
        }
        
        log.warn("[INVALID_STATE] {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of(e.getMessage(), "INVALID_STATE"));
    }
    
    /**
     * 💥 예상하지 못한 예외 (최후의 보루)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("예상하지 못한 에러 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.internalServerError("서버 내부 오류가 발생했습니다"));
    }
}
