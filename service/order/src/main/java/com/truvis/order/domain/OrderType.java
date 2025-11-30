package com.truvis.order.domain;

/**
 * 주문 유형
 */
public enum OrderType {
    /**
     * 시장가 주문 - 현재 시장 가격으로 즉시 체결
     */
    MARKET("시장가"),

    /**
     * 지정가 주문 - 지정한 가격에 도달하면 체결
     */
    LIMIT("지정가");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 시장가 주문인가?
     */
    public boolean isMarket() {
        return this == MARKET;
    }

    /**
     * 지정가 주문인가?
     */
    public boolean isLimit() {
        return this == LIMIT;
    }
}
