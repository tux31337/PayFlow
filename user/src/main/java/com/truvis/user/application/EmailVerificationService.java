package com.truvis.user.application;

import com.truvis.common.exception.EmailVerificationException;
import com.truvis.user.infrastructure.EmailSender;
import com.truvis.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailSender emailSender;
    
    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final String EMAIL_VERIFIED_PREFIX = "email:verified:";
    private static final Duration VERIFICATION_CODE_TTL = Duration.ofMinutes(10); // 인증번호 10분
    private static final Duration VERIFIED_EMAIL_TTL = Duration.ofMinutes(30);    // 인증완료 30분
    
    /**
     * 6자리 인증번호 기반 이메일 인증 요청
     */
    public String requestEmailVerificationCode(String email) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw EmailVerificationException.emailAlreadyExists(email);
        }
        
        // 2. 6자리 인증번호 생성
        String code = generateVerificationCode();
        
        // 3. Redis에 인증번호 저장 (이메일과 매핑)
        String codeKey = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, VERIFICATION_CODE_TTL);
        
        // 4. 실제 인증번호 이메일 전송
        try {
            emailSender.sendVerificationCodeEmail(email, code);
            log.info("이메일 인증번호 생성 및 메일 전송 완료: email={}, code={}", email, code);
        } catch (Exception e) {
            // 이메일 전송 실패시 Redis에서 인증번호 삭제
            redisTemplate.delete(codeKey);
            log.error("인증번호 이메일 전송 실패로 인증번호 삭제: email={}, code={}", email, code);
            throw EmailVerificationException.emailSendFailed(email);
        }
        
        return code; // 개발용으로 반환 (실제로는 반환하지 않음)
    }
    
    /**
     * 인증번호로 이메일 인증 완료
     */
    public String verifyEmailCode(String email, String code) {
        // 1. Redis에서 해당 이메일의 인증번호 조회
        String codeKey = EMAIL_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (storedCode == null) {
            throw EmailVerificationException.expiredCode();
        }
        
        if (!storedCode.equals(code)) {
            throw EmailVerificationException.invalidCode();
        }
        
        // 2. 인증 완료 상태로 변경 (30분간 유효)
        String verifiedKey = EMAIL_VERIFIED_PREFIX + email;
        redisTemplate.opsForValue().set(verifiedKey, "verified", VERIFIED_EMAIL_TTL);
        
        // 3. 사용된 인증번호 삭제
        redisTemplate.delete(codeKey);
        
        log.info("인증번호로 이메일 인증 완료: email={}", email);
        
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
     * 인증번호 재발송
     */
    public String resendVerificationCode(String email) {
        // 기존 인증번호 삭제
        String codeKey = EMAIL_CODE_PREFIX + email;
        redisTemplate.delete(codeKey);
        
        return requestEmailVerificationCode(email);
    }
    
    /**
     * 6자리 인증번호 생성
     */
    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
