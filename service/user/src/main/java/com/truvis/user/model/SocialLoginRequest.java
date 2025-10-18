package com.truvis.user.model;

import com.truvis.user.domain.SignUpType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 소셜 로그인 요청
 */
public record SocialLoginRequest(
        @NotNull(message = "소셜 프로바이더는 필수입니다")
        SignUpType provider,  // KAKAO, NAVER, GOOGLE

        @NotBlank(message = "Authorization Code는 필수입니다")
        String code
) {
}