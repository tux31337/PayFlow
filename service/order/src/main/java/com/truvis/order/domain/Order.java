package com.truvis.order.domain;

import com.truvis.common.model.AggregateRoot;
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
 * Order Aggregate Root
 * 
 * 주문은 실제 거래소처럼 동작:
 * 1. 주문 등록 시 PENDING 상태
 * 2. 시장 가격이 조건 충족 시 체결
 * 3. 체결 시 Transaction 생성 및 Portfolio 업데이트
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 낙관적 락(Optimistic Lock)을 위한 버전 관리
     */
    @Version
    private Long version;

    /**
     * 주문한 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 종목 코드
     */
    @Embedded
    private StockCode stockCode;

    /**
     * 주문 방향 (매수/매도)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side;

    /**
     * 주문 유형 (시장가/지정가)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderType type;

    /**
     * 주문 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /**
     * 주문 수량 (원하는 총 수량)
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity"))
    private Quantity quantity;

    /**
     * 체결된 수량 (실제로 체결된 수량)
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "filled_quantity"))
    private Quantity filledQuantity;

    /**
     * 남은 수량 (주문 수량 - 체결 수량)
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "remaining_quantity"))
    private Quantity remainingQuantity;

    /**
     * 지정가 (LIMIT 주문의 경우)
     * MARKET 주문의 경우 null
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "limit_price"))
    private Price limitPrice;

    /**
     * 평균 체결가 (여러 번에 걸쳐 체결될 수 있음)
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "average_price"))
    private Price averagePrice;

    /**
     * 총 체결 금액
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_amount"))
    private Money totalAmount;

    // createdAt, updatedAt은 BaseEntity에서 자동 관리

    /**
     * 주문 완료 시각 (체결/취소/거부)
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 취소 사유
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    // ==================== 생성자 ====================

    /**
     * Private 생성자 - 시장가 주문용
     */
    private Order(
            Long userId,
            StockCode stockCode,
            OrderSide side,
            Quantity quantity
    ) {
        validateOrderCreation(userId, stockCode, side, quantity);
        
        this.userId = userId;
        this.stockCode = stockCode;
        this.side = side;
        this.type = OrderType.MARKET;
        this.status = OrderStatus.PENDING;
        this.quantity = quantity;
        this.filledQuantity = Quantity.of(0);
        this.remainingQuantity = quantity;
        this.limitPrice = null;
        this.averagePrice = null;
        this.totalAmount = Money.ZERO;
    }

    /**
     * Private 생성자 - 지정가 주문용
     */
    private Order(
            Long userId,
            StockCode stockCode,
            OrderSide side,
            Quantity quantity,
            Price limitPrice
    ) {
        validateOrderCreation(userId, stockCode, side, quantity);
        Objects.requireNonNull(limitPrice, "지정가는 필수입니다");
        
        this.userId = userId;
        this.stockCode = stockCode;
        this.side = side;
        this.type = OrderType.LIMIT;
        this.status = OrderStatus.PENDING;
        this.quantity = quantity;
        this.filledQuantity = Quantity.of(0);
        this.remainingQuantity = quantity;
        this.limitPrice = limitPrice;
        this.averagePrice = null;
        this.totalAmount = Money.ZERO;
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 시장가 매수 주문 생성
     */
    public static Order createMarketBuyOrder(
            Long userId,
            StockCode stockCode,
            Quantity quantity
    ) {
        return new Order(userId, stockCode, OrderSide.BUY, quantity);
    }

    /**
     * 시장가 매도 주문 생성
     */
    public static Order createMarketSellOrder(
            Long userId,
            StockCode stockCode,
            Quantity quantity
    ) {
        return new Order(userId, stockCode, OrderSide.SELL, quantity);
    }

    /**
     * 지정가 매수 주문 생성
     */
    public static Order createLimitBuyOrder(
            Long userId,
            StockCode stockCode,
            Quantity quantity,
            Price limitPrice
    ) {
        return new Order(userId, stockCode, OrderSide.BUY, quantity, limitPrice);
    }

    /**
     * 지정가 매도 주문 생성
     */
    public static Order createLimitSellOrder(
            Long userId,
            StockCode stockCode,
            Quantity quantity,
            Price limitPrice
    ) {
        return new Order(userId, stockCode, OrderSide.SELL, quantity, limitPrice);
    }

    // ==================== 비즈니스 로직 ====================

    /**
     * 주문 체결 (부분 체결 가능)
     * 
     * @param filledQty 이번에 체결된 수량
     * @param filledPrice 체결 가격
     * @return 완전히 체결되었는지 여부
     */
    public boolean fill(Quantity filledQty, Price filledPrice) {
        validateFillable();
        validateFillQuantity(filledQty);
        Objects.requireNonNull(filledPrice, "체결 가격은 필수입니다");

        // 체결 수량 업데이트
        this.filledQuantity = this.filledQuantity.add(filledQty);
        this.remainingQuantity = this.quantity.subtract(this.filledQuantity);

        // 평균 체결가 계산
        updateAveragePrice(filledQty, filledPrice);

        // 총 체결 금액 업데이트
        this.totalAmount = this.averagePrice.multiply(this.filledQuantity);

        // 상태 업데이트
        if (this.remainingQuantity.isZero()) {
            // 완전 체결
            this.status = OrderStatus.FILLED;
            this.completedAt = LocalDateTime.now();
            return true;
        } else {
            // 부분 체결
            this.status = OrderStatus.PARTIALLY_FILLED;
            return false;
        }
    }

    /**
     * 주문 취소
     */
    public void cancel(String reason) {
        if (!status.isCancellable()) {
            throw new IllegalStateException(
                    String.format("주문을 취소할 수 없는 상태입니다: %s", status)
            );
        }

        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = reason;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 주문 거부
     */
    public void reject(String reason) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    String.format("PENDING 상태의 주문만 거부할 수 있습니다: %s", status)
            );
        }

        this.status = OrderStatus.REJECTED;
        this.cancellationReason = reason;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 지정가 주문의 체결 조건 확인
     * 
     * @param currentPrice 현재 시장 가격
     * @return 체결 가능 여부
     */
    public boolean canFillAtPrice(Price currentPrice) {
        if (type.isMarket()) {
            return true;
        }

        if (side.isBuy()) {
            return currentPrice.isLessThanOrEqual(limitPrice);
        } else {
            return currentPrice.isGreaterThanOrEqual(limitPrice);
        }
    }

    // ==================== 헬퍼 메서드 ====================

    /**
     * 평균 체결가 업데이트
     */
    private void updateAveragePrice(Quantity newFilledQty, Price newFilledPrice) {
        if (this.averagePrice == null) {
            this.averagePrice = newFilledPrice;
        } else {
            Money previousAmount = this.averagePrice.multiply(this.filledQuantity.subtract(newFilledQty));
            Money newAmount = newFilledPrice.multiply(newFilledQty);
            Money totalAmount = previousAmount.add(newAmount);
            this.averagePrice = totalAmount.divide(this.filledQuantity);
        }
        // updatedAt은 JPA Auditing이 자동 처리
    }

    /**
     * 주문 생성 유효성 검증
     */
    private void validateOrderCreation(
            Long userId,
            StockCode stockCode,
            OrderSide side,
            Quantity quantity
    ) {
        Objects.requireNonNull(userId, "사용자 ID는 필수입니다");
        Objects.requireNonNull(stockCode, "종목 코드는 필수입니다");
        Objects.requireNonNull(side, "주문 방향은 필수입니다");
        Objects.requireNonNull(quantity, "주문 수량은 필수입니다");
        
        if (quantity.isZero() || quantity.isNegative()) {
            throw new IllegalArgumentException("주문 수량은 0보다 커야 합니다");
        }
    }

    /**
     * 체결 가능 상태 검증
     */
    private void validateFillable() {
        if (!status.isActive()) {
            throw new IllegalStateException(
                    String.format("주문을 체결할 수 없는 상태입니다: %s", status)
            );
        }
    }

    /**
     * 체결 수량 검증
     */
    private void validateFillQuantity(Quantity filledQty) {
        if (filledQty.isZero() || filledQty.isNegative()) {
            throw new IllegalArgumentException("체결 수량은 0보다 커야 합니다");
        }
        
        if (filledQty.isGreaterThan(remainingQuantity)) {
            throw new IllegalArgumentException(
                    String.format(
                            "체결 수량(%d)이 남은 수량(%d)보다 큽니다",
                            filledQty.getValue(),
                            remainingQuantity.getValue()
                    )
            );
        }
    }

    // ==================== 조회 메서드 ====================

    public boolean isBuyOrder() {
        return side.isBuy();
    }

    public boolean isSellOrder() {
        return side.isSell();
    }

    public boolean isMarketOrder() {
        return type.isMarket();
    }

    public boolean isLimitOrder() {
        return type.isLimit();
    }

    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    public boolean isPartiallyFilled() {
        return status == OrderStatus.PARTIALLY_FILLED;
    }

    public boolean isFilled() {
        return status == OrderStatus.FILLED;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isCompleted() {
        return status.isCompleted();
    }

    /**
     * 주문 설명 문자열
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "[%s] %s %s %d주",
                type.getDisplayName(),
                side.getDisplayName(),
                stockCode.getValue(),
                quantity.getValue()
        ));
        
        if (type.isLimit()) {
            sb.append(String.format(" @ %s원", limitPrice.getValue()));
        }
        
        sb.append(String.format(" [%s]", status.getDisplayName()));
        
        if (status.isActive() || status == OrderStatus.PARTIALLY_FILLED) {
            sb.append(String.format(
                    " (체결: %d/%d)",
                    filledQuantity.getValue(),
                    quantity.getValue()
            ));
        }
        
        return sb.toString();
    }
}
