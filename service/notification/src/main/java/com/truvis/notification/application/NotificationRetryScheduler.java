package com.truvis.notification.application;

import com.truvis.notification.domain.Notification;
import com.truvis.notification.infrastructure.NotificationStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 알림 재시도 스케줄러
 * - 실패한 알림을 주기적으로 재시도
 * - 1분마다 실행
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationStatusRepository statusRepository;
    private final NotificationService notificationService;

    /**
     * 🔄 실패한 알림 재시도 (1분마다 실행)
     *
     * fixedDelay: 이전 작업 완료 후 60초 대기
     * initialDelay: 시작 후 10초 뒤부터 실행 (초기화 시간 확보)
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void retryFailedNotifications() {
        try {
            // 1. 실패한 알림 조회
            List<Notification> failedNotifications = statusRepository.findFailedNotifications();

            if (failedNotifications.isEmpty()) {
                log.debug("🔄 재시도 스케줄러 실행: 재시도 대상 없음");
                return;
            }

            log.info("🔄 재시도 스케줄러 시작: {} 건의 실패 알림 발견",
                    failedNotifications.size());

            // 2. 각 알림 재시도
            int retrySuccess = 0;
            int retryFailed = 0;
            int skipCount = 0;

            for (Notification notification : failedNotifications) {
                // 3. 재시도 가능 여부 확인
                if (!notification.canRetry()) {
                    log.warn("🚫 재시도 불가: id={}, retryCount={}/{}",
                            notification.getId(),
                            notification.getRetryCount(),
                            notification.getChannel().maxRetryCount());

                    skipCount++;

                    // TODO: Dead Letter Queue로 이동
                    // deadLetterQueueService.add(notification);
                    continue;
                }

                // 4. 재시도 실행
                try {
                    log.info("🔄 재시도 시도: id={}, retryCount={}",
                            notification.getId(),
                            notification.getRetryCount());

                    notificationService.send(notification);
                    retrySuccess++;

                    log.info("✅ 재시도 성공: id={}", notification.getId());

                } catch (Exception e) {
                    retryFailed++;
                    log.error("❌ 재시도 실패: id={}, error={}",
                            notification.getId(),
                            e.getMessage());

                    // 실패는 notificationService.send()에서 이미 처리됨
                    // (상태 FAILED로 변경, Redis 저장)
                }
            }

            // 5. 결과 로깅
            log.info("🔄 재시도 스케줄러 완료: 성공={}, 실패={}, 건너뜀={}",
                    retrySuccess, retryFailed, skipCount);

        } catch (Exception e) {
            log.error("🚨 재시도 스케줄러 예외: {}", e.getMessage(), e);
            // 예외가 발생해도 다음 스케줄은 계속 실행됨
        }
    }

    /**
     * 상태별 알림 개수 모니터링 (5분마다)
     * - 운영 모니터링용
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 30000)  // 5분
    public void monitorNotificationStatus() {
        try {
            long pendingCount = statusRepository.countByStatus(
                    com.truvis.notification.domain.NotificationStatus.PENDING);
            long sendingCount = statusRepository.countByStatus(
                    com.truvis.notification.domain.NotificationStatus.SENDING);
            long failedCount = statusRepository.countByStatus(
                    com.truvis.notification.domain.NotificationStatus.FAILED);
            long sentCount = statusRepository.countByStatus(
                    com.truvis.notification.domain.NotificationStatus.SENT);

            log.info("📊 알림 상태 모니터링: PENDING={}, SENDING={}, FAILED={}, SENT={}",
                    pendingCount, sendingCount, failedCount, sentCount);

            // 경고: FAILED가 너무 많으면
            if (failedCount > 100) {
                log.warn("⚠️ 실패 알림 과다: {} 건! 시스템 점검 필요", failedCount);
            }

            // 경고: SENDING이 오래 머물러 있으면 (스케줄러 2회 이상 지남)
            if (sendingCount > 10) {
                log.warn("⚠️ 발송 중 알림 과다: {} 건! 발송 지연 의심", sendingCount);
            }

        } catch (Exception e) {
            log.error("🚨 모니터링 예외: {}", e.getMessage(), e);
        }
    }
}