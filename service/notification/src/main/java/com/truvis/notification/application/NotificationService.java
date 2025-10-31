package com.truvis.notification.application;

import com.truvis.notification.domain.Notification;
import com.truvis.notification.domain.NotificationChannel;
import com.truvis.notification.domain.NotificationStatus;
import com.truvis.notification.infrastructure.NotificationProvider;
import com.truvis.notification.infrastructure.NotificationStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NotificationService {

    private final List<NotificationProvider> providers;
    private final NotificationStatusRepository statusRepository;

    /**
     *
     * @param providers
     * Spring이 모든 NotificationProvider 구현체를 자동으로 주입
     * - SmsNotificationProvider (나중에 추가하면 자동 포함)
     */
    public NotificationService(
            List<NotificationProvider> providers,
            NotificationStatusRepository statusRepository
    ) {
        this.providers = providers;
        this.statusRepository = statusRepository;

        log.info("📦 NotificationService 초기화: {} 개의 Provider 등록됨", providers.size());

        providers.forEach(provider ->
                log.info("  - {}", provider.getClass().getSimpleName())
        );
    }

    /**
     * 알림 발송
     */
    public void send(Notification notification) {
        try {
            // 0. 초기 상태(PENDING) Redis 저장
            statusRepository.save(notification);
            log.debug("알림 저장: id={}, status=PENDING", notification.getId());

            // 1. 발송 시작 상태로 변경
            NotificationStatus oldStatus = notification.getStatus();
            notification.startSending();
            statusRepository.moveStatus(notification, oldStatus);
            log.debug("알림 상태 변경: PENDING -> SENDING, id={}", notification.getId());

            // 2. 적절한 Provider 찾기
            NotificationProvider provider = findProvider(notification.getChannel());
            log.debug("Provider 선택: {} for id={}", 
                    provider.getClass().getSimpleName(), 
                    notification.getId());

            // 3. 실제 발송
            provider.send(notification);

            // 4. 발송 완료
            oldStatus = notification.getStatus();
            notification.markAsSent();
            statusRepository.moveStatus(notification, oldStatus);

            log.info("알림 발송 완료: id={}, type={}, duration={}ms",
                    notification.getId(),
                    notification.getType(),
                    notification.getSendingDurationMillis());

        } catch (Exception e) {
            // 5. 발송 실패
            NotificationStatus oldStatus = notification.getStatus();
            notification.markAsFailed(e.getMessage());
            statusRepository.moveStatus(notification, oldStatus);

            log.error("알림 발송 실패: id={}, type={}, error={}",
                    notification.getId(), 
                    notification.getType(),
                    e.getMessage());

            if (notification.canRetry()) {
                log.warn("알림 재시도 가능: id={}, retryCount={}/{}",
                        notification.getId(),
                        notification.getRetryCount(),
                        notification.getChannel().maxRetryCount());
            }

            throw new RuntimeException("알림 발송 실패", e);
        }
    }

    /**
     * 🔍 채널에 맞는 Provider 찾기
     *
     * @param channel 알림 채널
     * @return 해당 채널을 지원하는 Provider
     * @throws IllegalStateException 지원하는 Provider가 없을 때
     */
    private NotificationProvider findProvider(NotificationChannel channel) {
        return providers.stream()
                .filter(provider -> provider.supports(channel))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("지원하지 않는 알림 채널: %s (%s)",
                                channel.name(), channel.getDescription())
                ));
    }

    /**
     * 📊 등록된 Provider 수 조회 (테스트/디버깅용)
     */
    public int getProviderCount() {
        return providers.size();
    }

    /**
     * 📋 특정 채널 지원 여부 확인
     */
    public boolean isChannelSupported(NotificationChannel channel) {
        return providers.stream()
                .anyMatch(provider -> provider.supports(channel));
    }

}
