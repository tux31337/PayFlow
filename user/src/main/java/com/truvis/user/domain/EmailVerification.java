package com.truvis.user.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class EmailVerification {

    private final Email email;
    private final VerificationCode code;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private EmailVerificationStatus status;

    public static final int EXPIRATION_MINUTES = 10;

    @Builder
    private EmailVerification(
            Email email,
            VerificationCode code,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            EmailVerificationStatus status) {
        this.email = email;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    /**
     * 새로운 이메일 인증 생성 (팩토리 메서드)
     */
    public static EmailVerification create(Email email) {
        LocalDateTime now = LocalDateTime.now();

        return EmailVerification.builder()
                .email(email)
                .code(VerificationCode.generate())
                .createdAt(now)
                .expiresAt(now.plusMinutes(EXPIRATION_MINUTES))
                .status(EmailVerificationStatus.PENDING)
                .build();
    }

    /**
     * 인증번호 검증 및 인증 완료 처리
     *
     * 이게 핵심 비즈니스 로직!
     */
    public void verify(String inputCode) {
        // 1. 만료 확인
        if (isExpired()) {
            throw new IllegalStateException("인증번호가 만료되었습니다");
        }

        // 2. 이미 인증 완료된 경우
        if (this.status == EmailVerificationStatus.VERIFIED) {
            throw new IllegalStateException("이미 인증이 완료되었습니다");
        }

        // 3. 인증번호 일치 확인
        if (!this.code.matches(inputCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다");
        }

        // 4. 인증 완료 상태로 변경!
        this.status = EmailVerificationStatus.VERIFIED;
    }

    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 인증 완료 여부
     */
    public boolean isVerified() {
        return this.status == EmailVerificationStatus.VERIFIED;
    }

    /**
     * 인증 대기 중인지
     */
    public boolean isPending() {
        return this.status == EmailVerificationStatus.PENDING;
    }

    /**
     * 남은 시간 (분)
     */
    public long getRemainingMinutes() {
        if (isExpired()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(now, expiresAt).toMinutes();
    }

    /**
     * TTL 반환 (Repository에서 사용)
     */
    public Duration getTimeToLive() {
        return Duration.ofMinutes(EXPIRATION_MINUTES);
    }

    // 또는 static 메서드로
    public static Duration defaultTimeToLive() {
        return Duration.ofMinutes(EXPIRATION_MINUTES);
    }
}
