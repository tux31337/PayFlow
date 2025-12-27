package com.truvis.portfolio.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 보유 종목 정보 응답 DTO
 */
@Getter
@Builder
public class HoldingResponse {

    /**
     * 종목 코드
     */
    private String stockCode;

    /**
     * 보유 수량
     */
    private int quantity;

    /**
     * 평균 매수가
     */
    private BigDecimal averagePrice;

    /**
     * 현재가
     */
    private BigDecimal currentPrice;

    /**
     * 총 매수 금액
     */
    private BigDecimal totalCost;

    /**
     * 현재 평가액
     */
    private BigDecimal currentValue;

    /**
     * 평가 손익
     */
    private BigDecimal profit;

    /**
     * 수익률 (%)
     */
    private double returnRate;

    /**
     * 최초 매수 일시
     */
    private LocalDateTime firstPurchasedAt;
}

