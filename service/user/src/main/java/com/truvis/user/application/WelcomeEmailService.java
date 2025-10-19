package com.truvis.user.application;

import com.truvis.notification.domain.NotificationChannel;
import com.truvis.notification.domain.NotificationType;
import com.truvis.notification.event.NotificationRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 환영 메일 발송 서비스
 * - 회원가입 완료 시 환영 메일 발송
 */
@Service
@Slf4j
public class WelcomeEmailService {

    private final ApplicationEventPublisher eventPublisher;

    public WelcomeEmailService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 🎉 환영 메일 발송
     *
     * @param email 받는 사람 이메일
     * @param userName 사용자 이름
     */
    public void sendWelcomeEmail(String email, String userName) {
        try {
            NotificationRequestedEvent event = NotificationRequestedEvent.of(
                    email,
                    NotificationChannel.EMAIL,
                    NotificationType.WELCOME,
                    createWelcomeEmailContent(userName)
            );

            eventPublisher.publishEvent(event);

            log.info("✅ 환영 메일 발송 이벤트 발행 완료: email={}", email);

        } catch (Exception e) {
            // 환영 메일 실패해도 회원가입은 성공!
            log.error("⚠️ 환영 메일 이벤트 발행 실패 (무시): email={}, error={}",
                    email, e.getMessage());
            // 예외를 던지지 않음 → 회원가입은 정상 완료
        }
    }

    /**
     * 환영 메일 내용 생성
     */
    private String createWelcomeEmailContent(String userName) {
        return String.format("""
            안녕하세요, %s님!
            
            Truvis에 가입하신 것을 진심으로 환영합니다! 🎉
            
            Truvis는 당신의 투자 여정을 함께할 최고의 파트너입니다.
            
            ✨ 주요 기능
            - 실시간 포트폴리오 관리
            - 스마트 투자 분석
            - 맞춤형 투자 정보
            
            지금 바로 시작해보세요!
            
            궁금한 점이 있으시면 언제든지 문의해주세요.
            
            감사합니다.
            Truvis 팀
            """, userName);
    }
}