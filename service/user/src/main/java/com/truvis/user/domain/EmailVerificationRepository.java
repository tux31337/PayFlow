package com.truvis.user.domain;

import java.util.Optional;

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
