package com.truvis.notification.event;

import com.truvis.notification.application.NotificationService;
import com.truvis.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 알림 이벤트 리스너
 * - NotificationRequestedEvent를 비동기로 처리
 * - 실제 알림 발송을 담당
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * 🎯 알림 요청 이벤트 처리 (비동기!)
     *
     * @Async: 별도 스레드에서 실행 → 즉시 응답!
     * @EventListener: Spring 이벤트 자동 감지
     */
    @Async
    @EventListener
    public void handleNotificationRequested(NotificationRequestedEvent event) {
        log.info("📬 알림 이벤트 수신: type={}, channel={}, recipient={}",
                event.getType().getDescription(),
                event.getChannel().getDescription(),
                event.getRecipient());

        try {
            // 1. 도메인 객체 생성
            Notification notification = Notification.create(
                    event.getRecipient(),
                    event.getChannel(),
                    event.getType(),
                    event.getContent()
            );

            log.debug("알림 객체 생성: id={}, status={}",
                    notification.getId(),
                    notification.getStatus());

            // 2. 알림 발송 (NotificationService에 위임)
            notificationService.send(notification);

            log.info("✅ 알림 이벤트 처리 완료: id={}", notification.getId());

        } catch (Exception e) {
            log.error("❌ 알림 이벤트 처리 실패: type={}, recipient={}, error={}",
                    event.getType().getDescription(),
                    event.getRecipient(),
                    e.getMessage(), e);

            // TODO: 실패 시 재시도 로직 or 데드레터 큐
            // 지금은 로그만 남기고 계속 진행
        }
    }

    /**
     * 🔔 추가 이벤트 핸들러 예시
     * - 여러 이벤트 타입을 처리할 수 있음
     */
    // @Async
    // @EventListener
    // public void handleBulkNotification(BulkNotificationEvent event) {
    //     // 대량 발송 처리
    // }
}