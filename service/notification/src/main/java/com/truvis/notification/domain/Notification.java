package com.truvis.notification.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Getter
public class Notification {
    // 1. 식별자
    private final String id;              // 고유 ID

    // 2. 알림 기본 정보
    private final String recipient;        // 받는 사람 (이메일, 전화번호 등)
    private final NotificationChannel channel;
    private final NotificationType type;
    private final String content;          // 알림 내용

    // 3. 상태 관리
    private NotificationStatus status;     // 현재 상태

    // 4. 시간 정보
    private final LocalDateTime createdAt; // 생성 시간
    private LocalDateTime updatedAt;       // 수정 시간
    private LocalDateTime sentAt;          // 발송 완료 시간

    // 5. 실패 정보
    private String errorMessage;           // 실패 시 에러 메시지


    private int retryCount;                // 재시도 횟수

    @Builder
    private Notification(
            String id,
            String recipient,
            NotificationChannel channel,
            NotificationType type,
            String content,
            NotificationStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime sentAt,
            String errorMessage,
            int retryCount) {
        this.id = id;
        this.recipient = recipient;
        this.channel = channel;
        this.type = type;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.sentAt = sentAt;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
    }

    /**
     * 팩토리 메서드 : 새로운 알림 생성
     */
    public static Notification create(
            String recipient,
            NotificationChannel channel,
            NotificationType type,
            String content
    ) {
        LocalDateTime now = LocalDateTime.now();
        String id = UUID.randomUUID().toString();

        return Notification.builder()
                .id(id)
                .recipient(recipient)
                .channel(channel)
                .type(type)
                .content(content)
                .status(NotificationStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .retryCount(0)
                .build();

    }

    /**
     * 🔒 상태 변경 (검증 포함!)
     * - enum의 validateTransition() 호출
     * - 상태 변경
     * - updatedAt 갱신
     */
    private void updateStatus(NotificationStatus newStatus) {
        this.status.validateTransition(newStatus);
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();

        log.debug("알림 상태 변경: {} -> {} (id={})",
                this.status, newStatus, this.id);
    }

    /**
     * 발송 시작
     */
    public void startSending() {
        updateStatus(NotificationStatus.SENDING);  // PENDING → SENDING 검증
    }

    /**
     * 발송 완료
     */
    public void markAsSent() {
        updateStatus(NotificationStatus.SENT);  // SENDING → SENT 검증
        this.sentAt = LocalDateTime.now();
    }

    /**
     * 발송 실패
     */
    public void markAsFailed(String errorMessage) {
        updateStatus(NotificationStatus.FAILED);
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    /**
     * 재시도 가능한지
     */
    public boolean canRetry() {
        return this.status.canRetry() &&
                this.retryCount < this.channel.maxRetryCount();
    }

    /**
     * TTL 계산 (Redis 저장 시 사용)
     */
    public Duration getTimeToLive() {
        if (type.hasValidity()) {
            return Duration.ofMinutes(type.getValidityMinutes());
        }
        // 기본 TTL: 1시간
        return Duration.ofHours(1);
    }

    /**
     * 발송까지 걸린 시간 (ms)
     */
    public long getSendingDurationMillis() {
        if (sentAt == null) return 0;
        return Duration.between(createdAt, sentAt).toMillis();
    }

    /**
     * 발송 완료 여부
     */
    public boolean isSent() {
        return status == NotificationStatus.SENT;
    }

    /**
     * 발송 실패 여부
     */
    public boolean isFailed() {
        return status == NotificationStatus.FAILED;
    }

    /**
     * 처리 중인지 (PENDING 또는 SENDING)
     */
    public boolean isProcessing() {
        return status.isProcessing();
    }
}
