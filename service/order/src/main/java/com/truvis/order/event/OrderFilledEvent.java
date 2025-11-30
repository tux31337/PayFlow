package com.truvis.order.event;

import com.truvis.common.model.vo.StockCode;
import com.truvis.order.domain.OrderSide;

import java.time.LocalDateTime;

/**
 * 주문 체결 이벤트
 * 
 * 주문이 체결되면 발행되는 이벤트
 * Transaction 생성 및 Portfolio 업데이트를 트리거
 */
public record OrderFilledEvent(
        Long orderId,
        Long userId,
        StockCode stockCode,
        OrderSide side,
        int filledQuantity,    // 이번에 체결된 수량
        long filledPrice,      // 체결 가격
        boolean isFullyFilled, // 완전 체결 여부
        LocalDateTime filledAt
) {
    public static OrderFilledEvent of(
            Long orderId,
            Long userId,
            StockCode stockCode,
            OrderSide side,
            int filledQuantity,
            long filledPrice,
            boolean isFullyFilled
    ) {
        return new OrderFilledEvent(
                orderId,
                userId,
                stockCode,
                side,
                filledQuantity,
                filledPrice,
                isFullyFilled,
                LocalDateTime.now()
        );
    }
}
