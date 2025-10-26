package com.truvis.notification.infrastructure;

import com.truvis.notification.domain.Notification;
import com.truvis.notification.domain.NotificationChannel;
import com.truvis.notification.domain.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 이메일 알림 발송 구현체
 */
@Component
@Slf4j
public class EmailNotificationProvider implements NotificationProvider{

    @Autowired(required = false)  //
    private JavaMailSender mailSender;  // Spring의 이메일 발송 도구

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        // JavaMailSender가 없으면 콘솔 로그만 (개발용)
        if (mailSender == null) {
            logToConsole(notification);
            return;
        }

        try {
            SimpleMailMessage message = createEmailMessage(notification);
            mailSender.send(message);

            log.info("이메일 발송 완료: type={}, to={}, duration={}ms",
                    notification.getType(),
                    notification.getRecipient(),
                    notification.getSendingDurationMillis());

        } catch (Exception e) {
            log.error("이메일 발송 실패: type={}, to={}, error={}",
                    notification.getType(),
                    notification.getRecipient(), 
                    e.getMessage());
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 이메일 메시지 생성
     */
    private SimpleMailMessage createEmailMessage(Notification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.getRecipient());
        message.setSubject(createSubject(notification.getType()));
        message.setText(notification.getContent());
        message.setFrom("noreply@truvis.com");

        return message;
    }

    /**
     * 타입별 이메일 제목 생성
     */
    private String createSubject(NotificationType type) {
        return switch (type) {
            case VERIFICATION_CODE -> "[Truvis] 이메일 인증번호입니다";
            case PASSWORD_RESET -> "[Truvis] 비밀번호 재설정 안내";
            case WELCOME -> "[Truvis] 가입을 환영합니다!";
            case TRANSACTION_ALERT -> "[Truvis] 거래 알림";
            case MARKETING -> "[Truvis] 이벤트 안내";
        };
    }

    /**
     * 개발용: 콘솔에 이메일 내용 출력
     */
    private void logToConsole(Notification notification) {
        log.info("=== 개발 모드: 이메일 발송 ===");
        log.info("Type: {}", notification.getType());
        log.info("To: {}", notification.getRecipient());
        log.info("Subject: {}", createSubject(notification.getType()));
        log.info("==============================");
    }
}
