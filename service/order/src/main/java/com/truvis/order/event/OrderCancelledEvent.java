package com.truvis.order.event;

import java.time.LocalDateTime;

/**
 * 주문 취소 이벤트
 * 
 * 주문이 취소되면 발행되는 이벤트
 */
public record OrderCancelledEvent(
        Long orderId,
        Long userId,
        String reason,
        LocalDateTime cancelledAt
) {
    public static OrderCancelledEvent of(
            Long orderId,
            Long userId,
            String reason
    ) {
        return new OrderCancelledEvent(
                orderId,
                userId,
                reason,
                LocalDateTime.now()
        );
    }
}
