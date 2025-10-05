package com.truvis.user.model;

import java.time.LocalDateTime;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        LocalDateTime accessTokenExpiresAt,   // 액세스 토큰 만료 시간
        LocalDateTime refreshTokenExpiresAt   // 리프레시 토큰 만료 시간
) {
    public static LoginResponse of(
            String accessToken, 
            String refreshToken,
            LocalDateTime accessTokenExpiresAt,
            LocalDateTime refreshTokenExpiresAt) {
        return new LoginResponse(
                accessToken, 
                refreshToken, 
                "Bearer",
                accessTokenExpiresAt,
                refreshTokenExpiresAt
        );
    }
}