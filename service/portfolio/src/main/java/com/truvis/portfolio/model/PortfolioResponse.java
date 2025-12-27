package com.truvis.portfolio.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 포트폴리오 정보 응답 DTO
 */
@Getter
@Builder
public class PortfolioResponse {

    /**
     * 포트폴리오 ID
     */
    private Long portfolioId;

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 포트폴리오 이름
     */
    private String name;

    /**
     * 현금 잔고
     */
    private BigDecimal cashBalance;

    /**
     * 총 평가액 (보유 종목 현재 가치 합계)
     */
    private BigDecimal totalEvaluation;

    /**
     * 총 투자 금액 (매수 금액 합계)
     */
    private BigDecimal totalInvested;

    /**
     * 수익률 (%)
     */
    private double returnRate;

    /**
     * 총 자산 (현금 + 평가액)
     */
    private BigDecimal totalAssets;

    /**
     * 보유 종목 개수
     */
    private int holdingCount;

    /**
     * 보유 종목 목록
     */
    private List<HoldingResponse> holdings;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;
}
