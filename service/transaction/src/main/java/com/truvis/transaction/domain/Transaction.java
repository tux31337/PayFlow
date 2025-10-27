package com.truvis.transaction.domain;

import com.truvis.common.model.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 거래한 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 종목 코드
     */
    @Embedded
    private StockCode stockCode;

    /**
     * 거래 유형 (매수/매도)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;


    /**
     * 거래 수량
     */
    @Embedded
    private Quantity quantity;


    /**
     * 거래 단가
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price"))
    private Price price;

    /**
     * 거래 총액 (단가 × 수량)
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_amount"))
    private Money totalAmount;

    /**
     * 거래 실행 시각
     */
    @Column(nullable = false)
    private LocalDateTime executedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // private 생성자
    private Transaction(
            Long userId,
            StockCode stockCode,
            TransactionType type,
            Quantity quantity,
            Price price
    ) {
        this.userId = Objects.requireNonNull(userId, "사용자 ID는 필수입니다");
        this.stockCode = Objects.requireNonNull(stockCode, "종목 코드는 필수입니다");
        this.type = Objects.requireNonNull(type, "거래 유형은 필수입니다");
        this.quantity = Objects.requireNonNull(quantity, "수량은 필수입니다");
        this.price = Objects.requireNonNull(price, "가격은 필수입니다");
        this.totalAmount = calculateTotalAmount(price, quantity);
        this.executedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 정적 팩토리 메서드 - 거래 생성
     *
     * @return 생성된 거래 객체
     */
    public static Transaction create(
            Long userId,
            StockCode stockCode,
            TransactionType type,
            Quantity quantity,
            Price price
    ) {
        return new Transaction(userId, stockCode, type, quantity, price);
    }

    /**
     * 총액 계산 (단가 × 수량)
     */
    private Money calculateTotalAmount(Price price, Quantity quantity) {
        return price.multiply(quantity);
    }

    /**
     * 매수 거래인가?
     */
    public boolean isBuy() {
        return type.isBuy();
    }

    /**
     * 매도 거래인가?
     */
    public boolean isSell() {
        return type.isSell();
    }

    /**
     * 포트폴리오 수량 변화량 계산
     * - 매수: +수량
     * - 매도: -수량
     */
    public int getQuantityChange() {
        return quantity.getValue() * type.getQuantityMultiplier();
    }

    /**
     * 거래 설명 문자열
     */
    public String getDescription() {
        return String.format(
                "%s %s %d주 @ %s원 (총 %s원)",
                type.getDisplayName(),
                stockCode.getValue(),
                quantity.getValue(),
                price.getValue(),
                totalAmount.getValue()
        );
    }
}
