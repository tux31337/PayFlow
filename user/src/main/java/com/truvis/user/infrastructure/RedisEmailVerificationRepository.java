package com.truvis.user.infrastructure;

import com.truvis.user.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Slf4j
public class RedisEmailVerificationRepository implements EmailVerificationRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String CODE_PREFIX = "email:code:";
    private static final String VERIFIED_PREFIX = "email:verified:";
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);

    public RedisEmailVerificationRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(EmailVerification verification) {
        String key = CODE_PREFIX + verification.getEmail().getValue();

        // 데이터 직렬화: code|expiresAt|status
        String value = serialize(verification);

        // 도메인에서 TTL 가져옴!
        Duration ttl = EmailVerification.defaultTimeToLive();

        // Redis에 저장 (10분 TTL)
        redisTemplate.opsForValue().set(key, value, ttl);

        log.debug("EmailVerification 저장: email={}, code={}",
                verification.getEmail().getValue(),
                verification.getCode().getValue());
    }

    @Override
    public Optional<EmailVerification> findByEmail(Email email) {
        String key = CODE_PREFIX + email.getValue();
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            log.debug("EmailVerification 없음: email={}", email.getValue());
            return Optional.empty();
        }

        // 역직렬화
        EmailVerification verification = deserialize(email, value);

        log.debug("EmailVerification 조회: email={}, status={}",
                email.getValue(),
                verification.getStatus());

        return Optional.of(verification);
    }

    @Override
    public boolean existsVerifiedEmail(Email email) {
        String key = VERIFIED_PREFIX + email.getValue();
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void delete(Email email) {
        String codeKey = CODE_PREFIX + email.getValue();
        String verifiedKey = VERIFIED_PREFIX + email.getValue();

        redisTemplate.delete(codeKey);
        redisTemplate.delete(verifiedKey);

        log.debug("EmailVerification 삭제: email={}", email.getValue());
    }

    /**
     * EmailVerification을 문자열로 직렬화
     * 형식: code|expiresAt|status
     */
    private String serialize(EmailVerification verification) {
        return String.format("%s|%s|%s",
                verification.getCode().getValue(),
                verification.getExpiresAt().toString(),
                verification.getStatus().name());
    }

    /**
     * 문자열을 EmailVerification으로 역직렬화
     */
    private EmailVerification deserialize(Email email, String value) {
        String[] parts = value.split("\\|");

        if (parts.length != 3) {
            throw new IllegalStateException("Redis 데이터 형식이 올바르지 않습니다: " + value);
        }

        VerificationCode code = VerificationCode.of(parts[0]);
        LocalDateTime expiresAt = LocalDateTime.parse(parts[1]);
        EmailVerificationStatus status = EmailVerificationStatus.valueOf(parts[2]);

        // createdAt은 expiresAt - 10분으로 추정
        LocalDateTime createdAt = expiresAt.minusMinutes(10);

        return EmailVerification.builder()
                .email(email)
                .code(code)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .status(status)
                .build();
    }

    /**
     * 인증 완료 상태를 Redis에 저장 (30분 유효)
     */
    public void saveVerifiedStatus(Email email) {
        String key = VERIFIED_PREFIX + email.getValue();
        redisTemplate.opsForValue().set(key, "verified", VERIFIED_TTL);

        log.debug("인증 완료 상태 저장: email={}", email.getValue());
    }
}