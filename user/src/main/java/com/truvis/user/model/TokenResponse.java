package com.truvis.user.model;

import java.time.LocalDateTime;

public record TokenResponse(
        String accessToken,
        String tokenType,
        LocalDateTime expiresAt
) {
    public static TokenResponse of(String accessToken, LocalDateTime expiresAt) {
        return new TokenResponse(accessToken, "Bearer", expiresAt);
    }
}