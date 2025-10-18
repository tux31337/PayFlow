package com.truvis.user.application;

import com.truvis.common.exception.EmailVerificationException;
import com.truvis.user.domain.Email;
import com.truvis.user.domain.EmailVerification;
import com.truvis.user.domain.EmailVerificationRepository;
import com.truvis.user.infrastructure.EmailSender;
import com.truvis.user.infrastructure.RedisEmailVerificationRepository;
import com.truvis.user.domain.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository verificationRepository;
    private final EmailSender emailSender;

    public EmailVerificationService(
            UserRepository userRepository,
            EmailVerificationRepository verificationRepository,
            EmailSender emailSender) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.emailSender = emailSender;
    }

    /**
     * 이메일 인증번호 발송
     */
    @Transactional
    public void requestEmailVerificationCode(String emailValue) {
        // 1. Value Object 생성 (검증 포함)
        Email email = Email.of(emailValue);

        // 2. 이메일 중복 확인
        if (userRepository.existsByEmail(emailValue)) {
            throw EmailVerificationException.emailAlreadyExists(emailValue);
        }

        // 3. 도메인 객체 생성 (인증번호 자동 생성!)
        EmailVerification verification = EmailVerification.create(email);

        // 4. 저장
        verificationRepository.save(verification);

        // 5. 이메일 발송
        try {
            emailSender.sendVerificationCodeEmail(
                    email.getValue(),
                    verification.getCode().getValue()
            );
            log.info("인증번호 발송 완료: email={}", email.getValue());
        } catch (Exception e) {
            // 실패 시 롤백
            verificationRepository.delete(email);
            log.error("이메일 발송 실패: email={}", email.getValue(), e);
            throw EmailVerificationException.emailSendFailed(email.getValue());
        }
    }

    /**
     * 인증번호 검증
     */
    @Transactional
    public String verifyEmailCode(String emailValue, String codeValue) {
        // 1. Value Object 생성
        Email email = Email.of(emailValue);

        // 2. 도메인 객체 조회
        EmailVerification verification = verificationRepository.findByEmail(email)
                .orElseThrow(() -> EmailVerificationException.expiredCode());

        // 3. 도메인 객체에게 검증 요청! (Tell, Don't Ask!)
        try {
            verification.verify(codeValue);
        } catch (IllegalStateException e) {
            throw EmailVerificationException.expiredCode();
        } catch (IllegalArgumentException e) {
            throw EmailVerificationException.invalidCode();
        }

        // 4. 인증 완료 상태 저장 (VERIFIED로 변경)
        verificationRepository.save(verification);

        // ⭐ 5. 인증 완료 마크 저장
        if (verificationRepository instanceof RedisEmailVerificationRepository) {
            ((RedisEmailVerificationRepository) verificationRepository)
                    .saveVerifiedStatus(email);
            log.info("인증 완료 마크 저장: email={}", email.getValue());
        }

        log.info("이메일 인증 완료: email={}", email.getValue());

        // 6. 이메일 반환
        return email.getValue();
    }

    /**
     * 인증 완료 여부 확인
     */
    public boolean isEmailVerified(String emailValue) {
        Email email = Email.of(emailValue);
        return verificationRepository.existsVerifiedEmail(email);
    }

    /**
     * 인증 정보 삭제 (회원가입 완료 후)
     */
    @Transactional
    public void clearVerifiedEmail(String emailValue) {
        Email email = Email.of(emailValue);
        verificationRepository.delete(email);
        log.info("인증 정보 삭제: email={}", email.getValue());
    }

    /**
     * 인증번호 재발송
     */
    @Transactional
    public void resendVerificationCode(String emailValue) {
        Email email = Email.of(emailValue);

        // 기존 인증 정보 삭제
        verificationRepository.delete(email);

        // 새로 발송
        requestEmailVerificationCode(emailValue);
    }
}