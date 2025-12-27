package com.truvis.user.repository;

import com.truvis.user.domain.Email;
import com.truvis.user.domain.EmailVerification;

import java.util.Optional;

/**
 * EmailVerification Repository 인터페이스
 * - 이메일 인증 저장소 추상화
 * - 구현체: RedisEmailVerificationRepository (Redis)
 */
public interface EmailVerificationRepository {

    /**
     * 이메일 인증 저장
     */
    void save(EmailVerification verification);

    /**
     * 이메일로 인증 정보 조회
     */
    Optional<EmailVerification> findByEmail(Email email);

    /**
     * 인증 완료된 이메일인지 확인
     */
    boolean existsVerifiedEmail(Email email);

    /**
     * 이메일 인증 삭제
     */
    void delete(Email email);
}
