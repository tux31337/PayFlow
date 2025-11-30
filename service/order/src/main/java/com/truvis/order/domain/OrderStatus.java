package com.truvis.order.domain;

/**
 * 주문 상태
 */
public enum OrderStatus {
    /**
     * 대기 중 - 주문이 등록되었으나 아직 체결되지 않음
     */
    PENDING("대기"),

    /**
     * 부분 체결 - 주문의 일부만 체결됨
     */
    PARTIALLY_FILLED("부분체결"),

    /**
     * 체결 완료 - 주문이 완전히 체결됨
     */
    FILLED("체결완료"),

    /**
     * 취소됨 - 사용자가 주문을 취소함
     */
    CANCELLED("취소됨"),

    /**
     * 거부됨 - 시스템이 주문을 거부함 (잔고 부족 등)
     */
    REJECTED("거부됨");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 주문이 활성 상태인가?
     * - PENDING, PARTIALLY_FILLED 상태만 활성
     */
    public boolean isActive() {
        return this == PENDING || this == PARTIALLY_FILLED;
    }

    /**
     * 주문이 완료 상태인가?
     * - FILLED, CANCELLED, REJECTED 상태는 완료
     */
    public boolean isCompleted() {
        return this == FILLED || this == CANCELLED || this == REJECTED;
    }

    /**
     * 주문을 취소할 수 있는가?
     * - PENDING, PARTIALLY_FILLED 상태만 취소 가능
     */
    public boolean isCancellable() {
        return this == PENDING || this == PARTIALLY_FILLED;
    }
}
