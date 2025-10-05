package com.truvis.user.domain;

import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final String value;

    private Email(String value) {
        validate(value);
        this.value = value;
    }

    public static Email of(String value) {
        return new Email(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        Email email = (Email) o;
        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
