package com.truvis.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 📧 이메일 인증 도메인 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum EmailVerificationErrorCode implements ErrorCode {
    
    // 이메일 관련 (409 - 중복)
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다", HttpStatus.CONFLICT),
    
    // 인증번호 관련 (400)
    INVALID_CODE("INVALID_VERIFICATION_CODE", "인증번호가 일치하지 않습니다", HttpStatus.BAD_REQUEST),
    EXPIRED_CODE("EXPIRED_VERIFICATION_CODE", "인증번호가 만료되었습니다", HttpStatus.BAD_REQUEST),
    
    // 이메일 전송 실패 (503)
    EMAIL_SEND_FAILED("EMAIL_SEND_FAILED", "이메일 전송에 실패했습니다", HttpStatus.SERVICE_UNAVAILABLE, LogLevel.ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    private final LogLevel logLevel;
    
    EmailVerificationErrorCode(String code, String message, HttpStatus httpStatus) {
        this(code, message, httpStatus, LogLevel.WARN);
    }
}

