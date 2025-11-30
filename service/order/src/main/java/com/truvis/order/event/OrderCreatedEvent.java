package com.truvis.order.event;

import com.truvis.common.model.vo.StockCode;
import com.truvis.order.domain.OrderSide;
import com.truvis.order.domain.OrderType;

import java.time.LocalDateTime;

/**
 * 주문 생성 이벤트
 * 
 * 주문이 생성되면 발행되는 이벤트
 */
public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        StockCode stockCode,
        OrderSide side,
        OrderType type,
        int quantity,
        Long limitPrice, // null이면 시장가
        LocalDateTime createdAt
) {
    public static OrderCreatedEvent of(
            Long orderId,
            Long userId,
            StockCode stockCode,
            OrderSide side,
            OrderType type,
            int quantity,
            Long limitPrice
    ) {
        return new OrderCreatedEvent(
                orderId,
                userId,
                stockCode,
                side,
                type,
                quantity,
                limitPrice,
                LocalDateTime.now()
        );
    }
}
