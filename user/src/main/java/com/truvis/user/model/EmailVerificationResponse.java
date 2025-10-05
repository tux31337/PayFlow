package com.truvis.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationResponse {
    
    private boolean success;
    private String message;
    private String token;  // 인증 요청 시에만 사용
    
    // 성공 응답 생성 메서드
    public static EmailVerificationResponse success(String message) {
        return new EmailVerificationResponse(true, message, null);
    }
    
    public static EmailVerificationResponse success(String message, String token) {
        return new EmailVerificationResponse(true, message, token);
    }
    
    // 실패 응답 생성 메서드
    public static EmailVerificationResponse failure(String message) {
        return new EmailVerificationResponse(false, message, null);
    }
}
