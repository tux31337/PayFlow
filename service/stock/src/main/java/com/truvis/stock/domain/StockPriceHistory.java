package com.truvis.stock.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주식 가격 히스토리
 * - WebSocket 실시간 체결가 저장
 * - 분석, 차트, 백테스팅 용도
 */
@Entity
@Table(name = "stock_price_history",
        indexes = {
                @Index(name = "idx_stock_time", columnList = "stock_code,trade_time"),
                @Index(name = "idx_trade_time", columnList = "trade_time")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 종목코드
     */
    @Column(name = "stock_code", nullable = false, length = 10)
    private String stockCode;

    /**
     * 체결시각
     */
    @Column(name = "trade_time", nullable = false)
    private LocalDateTime tradeTime;

    /**
     * 현재가
     */
    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    /**
     * 전일대비 (원)
     */
    @Column(name = "price_change")
    private Long priceChange;

    /**
     * 전일대비율 (%)
     */
    @Column(name = "change_rate")
    private Double changeRate;

    /**
     * 체결거래량
     */
    @Column(name = "volume")
    private Long volume;

    /**
     * 데이터 저장 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public StockPriceHistory(
            String stockCode,
            LocalDateTime tradeTime,
            Long currentPrice,
            Long priceChange,
            Double changeRate,
            Long volume
    ) {
        this.stockCode = stockCode;
        this.tradeTime = tradeTime;
        this.currentPrice = currentPrice;
        this.priceChange = priceChange;
        this.changeRate = changeRate;
        this.volume = volume;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 팩토리 메서드 - 도메인 이벤트에서 생성
     */
    public static StockPriceHistory from(
            String stockCode,
            LocalDateTime tradeTime,
            Long currentPrice,
            Long priceChange,
            Double changeRate,
            Long volume
    ) {
        return StockPriceHistory.builder()
                .stockCode(stockCode)
                .tradeTime(tradeTime)
                .currentPrice(currentPrice)
                .priceChange(priceChange)
                .changeRate(changeRate)
                .volume(volume)
                .build();
    }
}
