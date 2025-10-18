package com.truvis.user.application;

import com.truvis.common.exception.EmailVerificationException;
import com.truvis.notification.domain.NotificationChannel;
import com.truvis.notification.domain.NotificationType;
import com.truvis.notification.event.NotificationRequestedEvent;
import com.truvis.user.domain.Email;
import com.truvis.user.domain.EmailVerification;
import com.truvis.user.domain.EmailVerificationRepository;
import com.truvis.user.domain.UserRepository;
import com.truvis.user.infrastructure.RedisEmailVerificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository verificationRepository;
    private final ApplicationEventPublisher eventPublisher;  // 🎯 변경!

    public EmailVerificationService(
            UserRepository userRepository,
            EmailVerificationRepository verificationRepository,
            ApplicationEventPublisher eventPublisher) {  // 🎯 변경!
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 이메일 인증번호 발송 (비동기!)
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

        // 5. 🎯 이메일 발송 이벤트 발행 (비동기!)
        NotificationRequestedEvent event = NotificationRequestedEvent.of(
                email.getValue(),
                NotificationChannel.EMAIL,
                NotificationType.VERIFICATION_CODE,
                createVerificationEmailContent(verification.getCode().getValue())
        );

        eventPublisher.publishEvent(event);

        log.info("✅ 인증번호 발송 이벤트 발행 완료: email={}", email.getValue());
        // 이제 즉시 반환! 이메일은 백그라운드에서 발송됨
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

        // 3. 도메인 객체에게 검증 요청!
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

    /**
     * 🎯 이메일 내용 생성 (private 헬퍼 메서드)
     */
    private String createVerificationEmailContent(String code) {
        return String.format("""
            안녕하세요, Truvis입니다.
            
            회원가입을 위한 이메일 인증번호입니다.
            
            인증번호: %s
            
            위 인증번호를 입력하여 이메일 인증을 완료해주세요.
            
            ※ 이 인증번호는 10분 동안 유효합니다.
            ※ 본인이 요청하지 않은 이메일이라면 무시하셔도 됩니다.
            
            감사합니다.
            Truvis 팀
            """, code);
    }
}