package com.truvis.user.domain;

/**
 * 회원가입 타입
 */
public enum SignUpType {
    EMAIL("이메일"),
    NAVER("네이버"),
    KAKAO("카카오"),
    GOOGLE("구글");
    
    private final String description;
    
    SignUpType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
