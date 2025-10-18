package com.truvis.common.security;

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    public RedisTokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addToBlacklist(String token, Duration ttl) {
        String key = BLACKLIST_PREFIX + token;

        // Redis에 저장 (value는 "blacklisted", TTL 설정)
        redisTemplate.opsForValue().set(key, "blacklisted", ttl);

        log.debug("토큰 블랙리스트 추가: token={}, ttl={}초",
                token, ttl.getSeconds());
    }


    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);

        boolean result = Boolean.TRUE.equals(exists);

        if (result) {
            log.debug("블랙리스트 토큰 감지: token={}", token);
        }

        return result;
    }

}
