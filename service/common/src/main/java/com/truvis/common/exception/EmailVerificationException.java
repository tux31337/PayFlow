package com.truvis.common.exception;

/**
 * 📧 이메일 인증 관련 예외
 * 
 * ErrorCode Enum 기반으로 HttpStatus가 자동으로 결정됩니다.
 */
public class EmailVerificationException extends BusinessException {
    
    public EmailVerificationException(EmailVerificationErrorCode errorCode) {
        super(errorCode);
    }
    
    public EmailVerificationException(EmailVerificationErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public EmailVerificationException(EmailVerificationErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    // ==================== 팩토리 메서드 ====================
    
    /**
     * 이미 가입된 이메일 (409)
     */
    public static EmailVerificationException emailAlreadyExists(String email) {
        return new EmailVerificationException(
                EmailVerificationErrorCode.EMAIL_ALREADY_EXISTS,
                "이미 가입된 이메일입니다: " + email
        );
    }
    
    /**
     * 인증번호 불일치 (400)
     */
    public static EmailVerificationException invalidCode() {
        return new EmailVerificationException(EmailVerificationErrorCode.INVALID_CODE);
    }
    
    /**
     * 인증번호 만료 (400)
     */
    public static EmailVerificationException expiredCode() {
        return new EmailVerificationException(EmailVerificationErrorCode.EXPIRED_CODE);
    }
    
    /**
     * 이메일 전송 실패 (503)
     */
    public static EmailVerificationException emailSendFailed(String email) {
        return new EmailVerificationException(
                EmailVerificationErrorCode.EMAIL_SEND_FAILED,
                "이메일 전송에 실패했습니다: " + email
        );
    }
}
