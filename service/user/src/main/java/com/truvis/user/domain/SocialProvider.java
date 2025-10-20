package com.truvis.user.domain;

import com.truvis.common.exception.MemberException;
import com.truvis.common.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 프로바이더 VO
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialProvider implements ValueObject {

    @Column(name = "social_provider", length = 20)
    private String value;

    private SocialProvider(String value) {
        this.value = value;
    }

    public static SocialProvider of(SignUpType signUpType) {
        if (signUpType == SignUpType.EMAIL) {
            throw MemberException.emailUserCannotHaveSocialProvider();
        }
        return new SocialProvider(signUpType.name());
    }

    public static SocialProvider kakao() {
        return new SocialProvider(SignUpType.KAKAO.name());
    }

    public static SocialProvider naver() {
        return new SocialProvider(SignUpType.NAVER.name());
    }

    public static SocialProvider google() {
        return new SocialProvider(SignUpType.GOOGLE.name());
    }

    public boolean isKakao() {
        return SignUpType.KAKAO.name().equals(this.value);
    }

    public boolean isNaver() {
        return SignUpType.NAVER.name().equals(this.value);
    }

    public boolean isGoogle() {
        return SignUpType.GOOGLE.name().equals(this.value);
    }
}