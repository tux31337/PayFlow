package com.truvis.user.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpResponse {

    private Long userId;
    private String email;
    private String name;

    private SignUpResponse(Long userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public static SignUpResponse of(Long userId, String email, String name) {
        return new SignUpResponse(userId, email, name);
    }
}