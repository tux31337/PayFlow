package com.truvis.portfolio.domain;

import com.truvis.common.model.vo.Money;
import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.common.model.vo.StockCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Holding Entity (보유 종목)
 * 
 * Portfolio의 일부로, 특정 종목의 보유 정보를 나타냄
 */
@Entity
@Table(name = "holdings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 종목 코드
     */
    @Embedded
    private StockCode stockCode;

    /**
     * 보유 수량
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity"))
    private Quantity quantity;

    /**
     * 평균 매수가
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "average_price"))
    private Price averagePrice;

    /**
     * 총 투자 금액 (평균 매수가 × 수량)
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_cost"))
    private Money totalCost;

    /**
     * 최초 매수 시각
     */
    @Column(name = "first_purchased_at", nullable = false)
    private LocalDateTime firstPurchasedAt;

    /**
     * 최종 수정 시각
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== 생성자 ====================

    private Holding(
            StockCode stockCode,
            Quantity quantity,
            Price averagePrice
    ) {
        this.stockCode = Objects.requireNonNull(stockCode, "종목 코드는 필수입니다");
        this.quantity = Objects.requireNonNull(quantity, "수량은 필수입니다");
        this.averagePrice = Objects.requireNonNull(averagePrice, "평균 매수가는 필수입니다");
        this.totalCost = averagePrice.multiply(quantity);
        this.firstPurchasedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 새 보유 종목 생성 (최초 매수)
     */
    public static Holding create(
            StockCode stockCode,
            Quantity quantity,
            Price price
    ) {
        return new Holding(stockCode, quantity, price);
    }

    // ==================== 비즈니스 로직 ====================

    /**
     * 추가 매수 (평균 단가 재계산)
     * 
     * @param additionalQuantity 추가 매수 수량
     * @param purchasePrice 매수 가격
     */
    public void buy(Quantity additionalQuantity, Price purchasePrice) {
        Objects.requireNonNull(additionalQuantity, "추가 수량은 필수입니다");
        Objects.requireNonNull(purchasePrice, "매수가는 필수입니다");

        // 기존 총 투자액
        Money previousCost = this.totalCost;
        
        // 추가 투자액
        Money additionalCost = purchasePrice.multiply(additionalQuantity);
        
        // 새로운 총 수량
        Quantity newQuantity = this.quantity.add(additionalQuantity);
        
        // 새로운 총 투자액
        Money newTotalCost = previousCost.add(additionalCost);
        
        // 새로운 평균 단가 = 총 투자액 / 총 수량
        Price newAveragePrice = newTotalCost.divide(newQuantity);
        
        // 업데이트
        this.quantity = newQuantity;
        this.averagePrice = newAveragePrice;
        this.totalCost = newTotalCost;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 매도 (수량 감소)
     * 
     * @param sellQuantity 매도 수량
     * @throws IllegalArgumentException 보유 수량보다 많이 매도하려는 경우
     */
    public void sell(Quantity sellQuantity) {
        Objects.requireNonNull(sellQuantity, "매도 수량은 필수입니다");

        if (!canSell(sellQuantity)) {
            throw new IllegalArgumentException(
                    String.format(
                            "보유 수량(%d)보다 많이 매도할 수 없습니다. 매도 요청: %d",
                            this.quantity.getValue(),
                            sellQuantity.getValue()
                    )
            );
        }

        // 새로운 수량
        Quantity newQuantity = this.quantity.subtract(sellQuantity);
        
        // 새로운 총 투자액 (평균 단가는 유지, 비례해서 감소)
        Money newTotalCost = this.averagePrice.multiply(newQuantity);
        
        // 업데이트
        this.quantity = newQuantity;
        this.totalCost = newTotalCost;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 매도 가능 여부 확인
     */
    public boolean canSell(Quantity sellQuantity) {
        return this.quantity.isGreaterThanOrEqual(sellQuantity);
    }

    /**
     * 보유 종목이 비어있는가?
     */
    public boolean isEmpty() {
        return this.quantity.isZero();
    }

    /**
     * 현재 평가액 계산
     * 
     * @param currentPrice 현재 시장 가격
     * @return 평가액 (현재가 × 보유 수량)
     */
    public Money calculateCurrentValue(Price currentPrice) {
        return currentPrice.multiply(this.quantity);
    }

    /**
     * 평가 손익 계산
     * 
     * @param currentPrice 현재 시장 가격
     * @return 평가 손익 (평가액 - 투자액)
     */
    public Money calculateProfit(Price currentPrice) {
        Money currentValue = calculateCurrentValue(currentPrice);
        return currentValue.subtract(this.totalCost);
    }

    /**
     * 수익률 계산
     * 
     * @param currentPrice 현재 시장 가격
     * @return 수익률 (%) 예: 15.5 (%)
     */
    public double calculateReturnRate(Price currentPrice) {
        if (this.totalCost.isZero()) {
            return 0.0;
        }

        Money profit = calculateProfit(currentPrice);
        
        // 수익률 = (평가 손익 / 투자액) × 100
        return profit.getValue().doubleValue() 
                / this.totalCost.getValue().doubleValue() 
                * 100.0;
    }

    /**
     * 보유 종목 설명
     */
    public String getDescription() {
        return String.format(
                "%s: %d주 @ 평균 %s원 (총 투자액: %s원)",
                stockCode.getValue(),
                quantity.getValue(),
                averagePrice.getValue(),
                totalCost.getValue()
        );
    }
}
