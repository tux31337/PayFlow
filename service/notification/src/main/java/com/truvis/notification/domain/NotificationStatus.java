package com.truvis.notification.domain;

public enum NotificationStatus {
    PENDING("발송 대기중", "이벤트가 발행되고 발송 대기 중인 상태"),
    SENDING("발송 중", "실제로 발송이 진행 중인 상태"),
    SENT("발송 완료", "성공적으로 발송이 완료된 상태"),
    FAILED("발송 실패", "발송에 실패한 상태");

    private final String description;
    private final String detail;

    NotificationStatus(String description, String detail) {
        this.description = description;
        this.detail = detail;
    }

    public String getDescription() {
        return description;
    }

    public String getDetail() {
        return detail;
    }

    /**
     * 🎯 핵심: 이 상태에서 다음 상태로 전이 가능한지 검증
     * <p>
     * 허용되는 전이:
     * PENDING → SENDING
     * SENDING → SENT
     * SENDING → FAILED
     */
    public boolean canTransitionTo(NotificationStatus next) {
        return switch (this) {
            case PENDING -> next == SENDING;
            case SENDING -> next == SENT || next == FAILED;
            case SENT, FAILED -> false;  // 최종 상태는 변경 불가
        };
    }

    /**
     * 🔒 상태 전이 검증 (예외 발생)
     *
     * @throws IllegalStateException 잘못된 상태 전이 시도 시
     */
    public void validateTransition(NotificationStatus next) {
        if (!canTransitionTo(next)) {
            throw new IllegalStateException(
                    String.format("잘못된 상태 전이: %s(%s) → %s(%s)",
                            this.name(), this.description,
                            next.name(), next.description)
            );
        }
    }

    /**
     * 최종 상태인지 확인 (더 이상 변경 불가)
     */
    public boolean isFinalState() {
        return this == SENT || this == FAILED;
    }

    /**
     * 성공 상태인지
     */
    public boolean isSuccess() {
        return this == SENT;
    }

    /**
     * 실패 상태인지
     */
    public boolean isFailure() {
        return this == FAILED;
    }

    /**
     * 처리 중인 상태인지 (PENDING 또는 SENDING)
     */
    public boolean isProcessing() {
        return this == PENDING || this == SENDING;
    }

    /**
     * 재시도 가능한 상태인지
     */
    public boolean canRetry() {
        return this == FAILED;
    }

    /**
     * 문자열로부터 상태 찾기
     */
    public static NotificationStatus fromString(String status) {
        try {
            return NotificationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 알림 상태: " + status);
        }
    }
}