package com.truvis.common.exception;

/**
 * 📋 Order 도메인 예외
 * 
 * ErrorCode Enum 기반으로 HttpStatus가 자동으로 결정됩니다.
 */
public class OrderException extends BusinessException {

    public OrderException(OrderErrorCode errorCode) {
        super(errorCode);
    }
    
    public OrderException(OrderErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public OrderException(OrderErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public OrderException(OrderErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode, customMessage, cause);
    }

    // ==================== 팩토리 메서드 ====================

    /**
     * 주문을 찾을 수 없음 (404)
     */
    public static OrderException notFound(Long orderId) {
        return new OrderException(
                OrderErrorCode.NOT_FOUND,
                String.format("주문을 찾을 수 없습니다. orderId: %d", orderId)
        );
    }

    /**
     * 주문 권한 없음 (403)
     */
    public static OrderException unauthorized(Long orderId) {
        return new OrderException(
                OrderErrorCode.UNAUTHORIZED,
                String.format("본인의 주문만 접근할 수 있습니다. orderId: %d", orderId)
        );
    }

    /**
     * 주문 취소 불가 상태 (400)
     */
    public static OrderException cannotCancel(Long orderId, String status) {
        return new OrderException(
                OrderErrorCode.CANNOT_CANCEL,
                String.format("취소할 수 없는 주문 상태입니다. orderId: %d, status: %s", orderId, status)
        );
    }

    /**
     * 잔고 부족 (400)
     */
    public static OrderException insufficientBalance(long currentBalance, long requiredAmount) {
        return new OrderException(
                OrderErrorCode.INSUFFICIENT_BALANCE,
                String.format("잔고가 부족하여 매수 주문을 생성할 수 없습니다. 현재 잔고: %,d원, 필요 금액: %,d원", 
                        currentBalance, requiredAmount)
        );
    }

    /**
     * 보유 수량 부족 (400)
     */
    public static OrderException insufficientHolding(String stockCode, int holdingQty, int requestedQty) {
        return new OrderException(
                OrderErrorCode.INSUFFICIENT_HOLDING,
                String.format("보유 수량이 부족하여 매도 주문을 생성할 수 없습니다. 종목: %s, 보유: %d주, 요청: %d주",
                        stockCode, holdingQty, requestedQty)
        );
    }

    /**
     * 지정가 필수 (400)
     */
    public static OrderException limitPriceRequired() {
        return new OrderException(OrderErrorCode.LIMIT_PRICE_REQUIRED);
    }

    /**
     * 예상 가격 필수 (400)
     */
    public static OrderException estimatedPriceRequired() {
        return new OrderException(OrderErrorCode.ESTIMATED_PRICE_REQUIRED);
    }

    /**
     * 이미 체결 완료 (400)
     */
    public static OrderException alreadyFilled(Long orderId) {
        return new OrderException(
                OrderErrorCode.ALREADY_FILLED,
                String.format("이미 체결 완료된 주문입니다. orderId: %d", orderId)
        );
    }
}
