package com.truvis.stock.infrastructure.kis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 한국투자증권 API 토큰 발급 응답
 */
@Getter
@NoArgsConstructor
@ToString
public class KisTokenResponse {

    /**
     * Access Token
     * - API 호출 시 사용
     * - 유효기간: 24시간
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 토큰 타입 (Bearer)
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * 만료 시간 (초)
     * - 보통 86400 (24시간)
     */
    @JsonProperty("expires_in")
    private int expiresIn;

    /**
     * 토큰 발급 시각 (로컬)
     */
    private LocalDateTime issuedAt = LocalDateTime.now();

    /**
     * 토큰이 만료되었는가?
     * - 안전하게 1시간 일찍 만료 처리
     */
    public boolean isExpired() {
        LocalDateTime expiryTime = issuedAt.plusSeconds(expiresIn - 3600);
        return LocalDateTime.now().isAfter(expiryTime);
    }
}