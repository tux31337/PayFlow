package com.truvis.user.domain;

import lombok.Getter;

@Getter
public class VerificationCode {
    private static final int CODE_LENGTH = 6;

    private final String value;

    private VerificationCode(String value) {
        validate(value);
        this.value = value;
    }

    /**
     * 문자열로부터 인증번호 생성
     */
    public static VerificationCode of(String value) {
        return new VerificationCode(value);
    }

    /**
     * 6자리 랜덤 인증번호 생성
     */
    public static VerificationCode generate() {
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        return new VerificationCode(code);
    }

    /**
     * 입력된 코드와 일치하는지 확인
     */
    public boolean matches(String inputCode) {
        return this.value.equals(inputCode);
    }

    private void validate(String value) {
        if (value == null || value.length() != CODE_LENGTH) {
            throw new IllegalArgumentException("인증번호는 6자리여야 합니다");
        }
        if (!value.matches("\\d{6}")) {
            throw new IllegalArgumentException("인증번호는 숫자만 가능합니다");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerificationCode)) return false;
        VerificationCode that = (VerificationCode) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
