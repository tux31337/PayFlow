package com.truvis.common.security;

import java.time.Duration;

/**
 * 토큰 블랙리스트 서비스
 * 
 * 로그아웃된 토큰을 블랙리스트에 추가하고 검증하는 책임을 가진다.
 * JWT는 stateless하므로, 로그아웃된 토큰을 별도로 관리해야 한다.
 */
public interface TokenBlacklistService {
    /**
     * 토큰을 블랙리스트에 추가 (무효화)
     *
     * @param token 무효화할 AccessToken
     * @param ttl 블랙리스트 유지 시간 (토큰의 남은 유효 시간만큼만 보관)
     */
    void addToBlacklist(String token, Duration ttl);

    /**
     * 토큰이 블랙리스트에 있는지 확인
     *
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true (무효화된 토큰)
     */
    boolean isBlacklisted(String token);
}
