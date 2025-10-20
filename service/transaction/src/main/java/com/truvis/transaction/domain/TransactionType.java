package com.truvis.transaction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 거래 유형
 * - BUY: 매수
 * - SELL: 매도
 */
@Getter
@RequiredArgsConstructor
public enum TransactionType {

    BUY("매수", "주식을 구매하는 거래"),
    SELL("매도", "주식을 판매하는 거래");

    private final String displayName;
    private final String description;

    /**
     * 매수인가?
     */
    public boolean isBuy() {
        return this == BUY;
    }

    /**
     * 매도인가?
     */
    public boolean isSell() {
        return this == SELL;
    }

    /**
     * 수량에 적용할 부호
     * - 매수: +1 (수량 증가)
     * - 매도: -1 (수량 감소)
     */
    public int getQuantityMultiplier() {
        return this == BUY ? 1 : -1;
    }
}