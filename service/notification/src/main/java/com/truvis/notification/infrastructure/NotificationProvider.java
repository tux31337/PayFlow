package com.truvis.notification.infrastructure;

import com.truvis.notification.domain.Notification;
import com.truvis.notification.domain.NotificationChannel;

/**
 * 알림 발송 제공자 인터페이스
 */
public interface NotificationProvider {
    /**
     * 이 Provider가 지원하는 채널인지 확인
     *
     * @param channel 알림 채널
     * @return 지원 여부
     */
    boolean supports(NotificationChannel channel);

    /**
     * 알림 발송
     *
     * @param notification 발송할 알림
     * @throws NotificationSendException 발송 실패 시
     */
    void send(Notification notification);

}
