package com.truvis.member.application;

import com.truvis.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String EMAIL_VERIFICATION_PREFIX = "email:verification:";
    private static final String EMAIL_VERIFIED_PREFIX = "email:verified:";
    private static final Duration VERIFICATION_TOKEN_TTL = Duration.ofHours(24); // 24시간
    private static final Duration VERIFIED_EMAIL_TTL = Duration.ofMinutes(30);   // 30분
    
    /**
     * 이메일 인증 요청
     */
    public String requestEmailVerification(String email) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + email);
        }
        
        // 2. 인증 토큰 생성
        String token = generateVerificationToken();
        
        // 3. Redis에 토큰 저장 (이메일과 매핑)
        String tokenKey = EMAIL_VERIFICATION_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, email, VERIFICATION_TOKEN_TTL);
        
        log.info("이메일 인증 토큰 생성: email={}, token={}", email, token);
        
        return token;
    }
    
    /**
     * 이메일 인증 완료
     */
    public String verifyEmail(String token) {
        // 1. 토큰으로 이메일 조회
        String tokenKey = EMAIL_VERIFICATION_PREFIX + token;
        String email = redisTemplate.opsForValue().get(tokenKey);
        
        if (email == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 인증 토큰입니다");
        }
        
        // 2. 인증 완료 상태로 변경 (30분간 유효)
        String verifiedKey = EMAIL_VERIFIED_PREFIX + email;
        redisTemplate.opsForValue().set(verifiedKey, "verified", VERIFIED_EMAIL_TTL);
        
        // 3. 사용된 토큰 삭제
        redisTemplate.delete(tokenKey);
        
        log.info("이메일 인증 완료: email={}", email);
        
        return email;
    }
    
    /**
     * 이메일 인증 완료 여부 확인
     */
    public boolean isEmailVerified(String email) {
        String verifiedKey = EMAIL_VERIFIED_PREFIX + email;
        String verified = redisTemplate.opsForValue().get(verifiedKey);
        return "verified".equals(verified);
    }
    
    /**
     * 인증 완료된 이메일 정리 (회원가입 완료 후 호출)
     */
    public void clearVerifiedEmail(String email) {
        String verifiedKey = EMAIL_VERIFIED_PREFIX + email;
        redisTemplate.delete(verifiedKey);
        log.info("인증 완료 이메일 정리: email={}", email);
    }
    
    /**
     * 인증 토큰 재발송
     */
    public String resendVerificationToken(String email) {
        // 기존 토큰들 정리
        // TODO: 기존 토큰을 찾아서 삭제하는 로직 필요 (현재는 단순 재생성)
        
        return requestEmailVerification(email);
    }
    
    /**
     * UUID 기반 인증 토큰 생성
     */
    private String generateVerificationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
