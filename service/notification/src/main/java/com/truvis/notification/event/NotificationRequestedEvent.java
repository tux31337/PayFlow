package com.truvis.notification.event;

import com.truvis.common.model.DomainEvent;
import com.truvis.notification.domain.NotificationChannel;
import com.truvis.notification.domain.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 알림 발송 요청 이벤트
 * - 알림이 필요할 때 이 이벤트를 발행
 * - EventListener가 비동기로 처리
 */
@Getter
@ToString
@Builder
public class NotificationRequestedEvent extends DomainEvent {
    /**
     * 받는 사람 (이메일, 전화번호 등)
     */
    private final String recipient;

    /**
     * 알림 채널 (EMAIL, SMS, ...)
     */
    private final NotificationChannel channel;

    /**
     * 알림 타입 (VERIFICATION_CODE, ...)
     */
    private final NotificationType type;

    /**
     * 알림 내용
     */
    private final String content;

    /**
     * 이벤트 발행 시간
     */
    private final LocalDateTime requestedAt;

    /**
     * 요청자 정보 (선택)
     */
    private final String requestedBy;

    /**
     * 🏭 정적 팩토리 메서드 (편의성)
     */
    public static NotificationRequestedEvent of(
            String recipient,
            NotificationChannel channel,
            NotificationType type,
            String content) {

        return NotificationRequestedEvent.builder()
                .recipient(recipient)
                .channel(channel)
                .type(type)
                .content(content)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 요청자 정보 포함 버전
     */
    public static NotificationRequestedEvent of(
            String recipient,
            NotificationChannel channel,
            NotificationType type,
            String content,
            String requestedBy) {

        return NotificationRequestedEvent.builder()
                .recipient(recipient)
                .channel(channel)
                .type(type)
                .content(content)
                .requestedAt(LocalDateTime.now())
                .requestedBy(requestedBy)
                .build();
    }
}
