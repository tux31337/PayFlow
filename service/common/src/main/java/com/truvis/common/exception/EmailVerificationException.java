package com.truvis.common.exception;

/**
 * 이메일 인증 관련 예외
 */
public class EmailVerificationException extends BusinessException {
    
    public EmailVerificationException(String message) {
        super("EMAIL_VERIFICATION_ERROR", message);
    }
    
    public EmailVerificationException(String message, Throwable cause) {
        super("EMAIL_VERIFICATION_ERROR", message, cause);
    }
    
    public EmailVerificationException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public static EmailVerificationException emailAlreadyExists(String email) {
        return new EmailVerificationException("EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다: " + email);
    }
    
    public static EmailVerificationException invalidCode() {
        return new EmailVerificationException("INVALID_VERIFICATION_CODE", "인증번호가 일치하지 않습니다");
    }
    
    public static EmailVerificationException expiredCode() {
        return new EmailVerificationException("EXPIRED_VERIFICATION_CODE", "인증번호가 만료되었습니다");
    }
    
    public static EmailVerificationException emailSendFailed(String email) {
        return new EmailVerificationException("EMAIL_SEND_FAILED", "이메일 전송에 실패했습니다: " + email);
    }
}
