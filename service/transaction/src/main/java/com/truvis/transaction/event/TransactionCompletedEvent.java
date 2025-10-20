package com.truvis.transaction.event;

import com.truvis.common.model.DomainEvent;
import com.truvis.transaction.domain.Transaction;
import com.truvis.transaction.domain.TransactionType;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
public class TransactionCompletedEvent extends DomainEvent {
    /**
     * 거래 ID
     */
    private final Long transactionId;

    /**
     * 사용자 ID
     */
    private final Long userId;

    /**
     * 종목 코드
     */
    private final String stockCode;

    /**
     * 거래 유형 (BUY, SELL)
     */
    private final TransactionType type;

    /**
     * 거래 수량
     */
    private final int quantity;

    /**
     * 거래 단가
     */
    private final BigDecimal price;

    /**
     * 거래 총액
     */
    private final BigDecimal totalAmount;

    /**
     * 거래 실행 시각
     */
    private final LocalDateTime executedAt;

    /**
     * 수량 변화량 (매수: +, 매도: -)
     */
    private final int quantityChange;


    private TransactionCompletedEvent(
            Long transactionId,
            Long userId,
            String stockCode,
            TransactionType type,
            int quantity,
            BigDecimal price,
            BigDecimal totalAmount,
            LocalDateTime executedAt,
            int quantityChange
    ) {
        super();  // DomainEvent의 occurredOn 설정
        this.transactionId = transactionId;
        this.userId = userId;
        this.stockCode = stockCode;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
        this.executedAt = executedAt;
        this.quantityChange = quantityChange;
    }

    /**
     * 정적 팩토리 메서드 - Transaction으로부터 생성
     */
    public static TransactionCompletedEvent of(Transaction transaction) {
        return new TransactionCompletedEvent(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getStockCode().getValue(),
                transaction.getType(),
                transaction.getQuantity().getValue(),
                transaction.getPrice().getValue(),
                transaction.getTotalAmount().getValue(),
                transaction.getExecutedAt(),
                transaction.getQuantityChange()
        );
    }

    /**
     * 매수 거래인가?
     */
    public boolean isBuyTransaction() {
        return type.isBuy();
    }

    /**
     * 매도 거래인가?
     */
    public boolean isSellTransaction() {
        return type.isSell();
    }

    /**
     * 이벤트 설명
     */
    public String getDescription() {
        return String.format(
                "[거래완료] %s - %s %s %d주 @ %s원 (총 %s원)",
                userId,
                type.getDisplayName(),
                stockCode,
                quantity,
                price,
                totalAmount
        );
    }
}
