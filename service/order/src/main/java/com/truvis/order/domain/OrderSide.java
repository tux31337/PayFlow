package com.truvis.order.domain;

/**
 * 주문 방향 (매수/매도)
 */
public enum OrderSide {
    /**
     * 매수 - 주식을 사는 주문
     */
    BUY("매수", 1),

    /**
     * 매도 - 주식을 파는 주문
     */
    SELL("매도", -1);

    private final String displayName;
    private final int quantityMultiplier;

    OrderSide(String displayName, int quantityMultiplier) {
        this.displayName = displayName;
        this.quantityMultiplier = quantityMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 수량 변화 계산용 승수
     * - 매수: +1 (포트폴리오에 추가)
     * - 매도: -1 (포트폴리오에서 차감)
     */
    public int getQuantityMultiplier() {
        return quantityMultiplier;
    }

    /**
     * 매수 주문인가?
     */
    public boolean isBuy() {
        return this == BUY;
    }

    /**
     * 매도 주문인가?
     */
    public boolean isSell() {
        return this == SELL;
    }
}
