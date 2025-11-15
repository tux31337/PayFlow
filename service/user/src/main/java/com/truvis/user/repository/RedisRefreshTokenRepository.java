package com.truvis.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@Slf4j
public class RedisRefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final long REFRESH_TOKEN_TTL_DAYS = 14;  // 14일

    public RedisRefreshTokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * RefreshToken 저장
     * @param userId 사용자 ID
     * @param refreshToken 리프레시 토큰 값
     */
    public void save(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_TTL_DAYS, TimeUnit.DAYS);

        log.debug("RefreshToken 저장: userId={}", userId);
    }

    /**
     * RefreshToken 조회
     * @param userId 사용자 ID
     * @return 저장된 RefreshToken (없으면 null)
     */
    public String findByUserId(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String token = redisTemplate.opsForValue().get(key);

        log.debug("RefreshToken 조회: userId={}, exists={}", userId, token != null);
        return token;
    }

    /**
     * RefreshToken 삭제 (로그아웃)
     * @param userId 사용자 ID
     */
    public void delete(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);

        log.debug("RefreshToken 삭제: userId={}", userId);
    }
}