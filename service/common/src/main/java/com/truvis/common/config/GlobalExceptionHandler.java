package com.truvis.common.config;

import com.truvis.common.exception.BusinessException;
import com.truvis.common.exception.EmailVerificationException;
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
 * 모든 Controller에서 발생하는 예외를 여기서 한 번에 처리해요!
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 */
@RestControllerAdvice  
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 1️⃣ 이메일 인증 관련 예외 처리
     */
    @ExceptionHandler(EmailVerificationException.class)
    public ResponseEntity<ErrorResponse> handleEmailVerificationException(EmailVerificationException e) {
        log.warn("이메일 인증 예외 발생: {}", e.getMessage());
        
        ErrorResponse errorResponse;
        if (e.getMessage().contains("이미 가입된")) {
            errorResponse = ErrorResponse.emailAlreadyExists(e.getMessage().split(": ")[1]);
        } else if (e.getMessage().contains("일치하지 않습니다")) {
            errorResponse = ErrorResponse.invalidVerificationCode();
        } else if (e.getMessage().contains("만료되었습니다")) {
            errorResponse = ErrorResponse.expiredVerificationCode();
        } else {
            errorResponse = ErrorResponse.badRequest(e.getMessage());
        }
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * 2️⃣ 비즈니스 예외 처리 (일반적인 비즈니스 로직 에러)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("비즈니스 예외 발생: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ErrorResponse.badRequest(e.getMessage()));
    }
    
    /**
     * 3️⃣ Validation 예외 처리 (@Valid 어노테이션에서 발생)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        
        // 모든 필드 에러를 하나의 메시지로 합치기
        String message = bindingResult.getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        log.warn("Validation 예외 발생: {}", message);
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of(message, "VALIDATION_ERROR"));
    }
    
    /**
     * 4️⃣ IllegalArgumentException 처리 (잘못된 파라미터)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 파라미터: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ErrorResponse.badRequest(e.getMessage()));
    }
    
    /**
     * 5️⃣ 예상하지 못한 모든 예외 처리 (최후의 보루)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("예상하지 못한 에러 발생", e);  // 스택트레이스까지 로깅
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.internalServerError("서버 내부 오류가 발생했습니다"));
    }
}
