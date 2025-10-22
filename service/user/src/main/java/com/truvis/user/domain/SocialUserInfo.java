package com.truvis.user.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialUserInfo {
    private final String socialId;        // 소셜 고유 ID (예: 카카오 "1234567890")
    private final String email;           // 이메일
    private final String name;            // 이름
    private final SignUpType provider;    // 소셜 프로바이더 (KAKAO/NAVER/GOOGLE)

    public static SocialUserInfo of(String socialId, String email, String name, SignUpType provider) {
        return SocialUserInfo.builder()
                .socialId(socialId)
                .email(email)
                .name(name)
                .provider(provider)
                .build();
    }
}
