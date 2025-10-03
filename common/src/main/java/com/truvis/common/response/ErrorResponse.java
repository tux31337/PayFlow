package com.truvis.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private boolean success = false;        // 항상 false
    private String message;                 // 에러 메시지
    private String errorCode;              // 에러 코드 (예: "EMAIL_ALREADY_EXISTS")
    private long timestamp;                // 에러 발생 시간
    
    // 간편 생성 메서드들
    public static ErrorResponse of(String message, String errorCode) {
        return new ErrorResponse(false, message, errorCode, System.currentTimeMillis());
    }
    
    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, message, "UNKNOWN_ERROR", System.currentTimeMillis());
    }
    
    // 자주 사용하는 에러들
    public static ErrorResponse badRequest(String message) {
        return of(message, "BAD_REQUEST");
    }
    
    public static ErrorResponse notFound(String message) {
        return of(message, "NOT_FOUND");
    }
    
    public static ErrorResponse internalServerError(String message) {
        return of(message, "INTERNAL_SERVER_ERROR");
    }
    
    public static ErrorResponse emailAlreadyExists(String email) {
        return of("이미 가입된 이메일입니다: " + email, "EMAIL_ALREADY_EXISTS");
    }
    
    public static ErrorResponse invalidVerificationCode() {
        return of("인증번호가 일치하지 않습니다", "INVALID_VERIFICATION_CODE");
    }
    
    public static ErrorResponse expiredVerificationCode() {
        return of("인증번호가 만료되었습니다", "EXPIRED_VERIFICATION_CODE");
    }
}
