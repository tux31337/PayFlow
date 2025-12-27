package com.truvis.portfolio.model;

import com.truvis.common.model.vo.Money;
import lombok.Builder;
import lombok.Getter;

/**
 * 포트폴리오 요약 정보
 * - 내부 서비스 간 데이터 전달용
 */
@Getter
@Builder
public class PortfolioSummary {

    /**
     * 포트폴리오 ID
     */
    private Long portfolioId;

    /**
     * 포트폴리오 이름
     */
    private String name;

    /**
     * 현금 잔고
     */
    private Money cashBalance;

    /**
     * 총 평가액 (보유 종목 현재 가치 합계)
     */
    private Money totalEvaluation;

    /**
     * 총 투자 금액
     */
    private Money totalInvested;

    /**
     * 총 수익
     */
    private Money totalProfit;

    /**
     * 총 손실 (손실인 경우)
     */
    private Money totalLoss;

    /**
     * 수익률 (%)
     */
    private double returnRate;

    /**
     * 총 자산 (현금 + 평가액)
     */
    private Money totalAssets;

    /**
     * 보유 종목 개수
     */
    private int holdingCount;

    /**
     * 수익/손실 여부
     */
    public boolean isProfitable() {
        return returnRate >= 0;
    }

    /**
     * 포트폴리오 요약 문자열
     */
    public String getSummaryText() {
        String profitOrLoss = isProfitable() 
                ? String.format("+%s원 (+%.2f%%)", totalProfit.getValue(), returnRate)
                : String.format("-%s원 (%.2f%%)", totalLoss.getValue(), returnRate);

        return String.format(
                "[%s] 총 자산: %s원 | 현금: %s원 | 평가액: %s원 | 손익: %s | 종목: %d개",
                name,
                totalAssets.getValue(),
                cashBalance.getValue(),
                totalEvaluation.getValue(),
                profitOrLoss,
                holdingCount
        );
    }
}

