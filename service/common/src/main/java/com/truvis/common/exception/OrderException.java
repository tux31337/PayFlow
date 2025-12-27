package com.truvis.common.exception;

/**
 * Order 도메인 예외
 */
public class OrderException extends BusinessException {

    public OrderException(String errorCode, String message) {
        super(errorCode, message);
    }

    public OrderException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    // ==================== 팩토리 메서드 ====================

    /**
     * 주문을 찾을 수 없음
     */
    public static OrderException notFound(Long orderId) {
        return new OrderException(
                "ORDER_001",
                String.format("주문을 찾을 수 없습니다. orderId: %d", orderId)
        );
    }

    /**
     * 주문 권한 없음 (본인 주문이 아님)
     */
    public static OrderException unauthorized(Long orderId) {
        return new OrderException(
                "ORDER_002",
                String.format("본인의 주문만 접근할 수 있습니다. orderId: %d", orderId)
        );
    }

    /**
     * 주문 취소 불가 상태
     */
    public static OrderException cannotCancel(Long orderId, String status) {
        return new OrderException(
                "ORDER_003",
                String.format("취소할 수 없는 주문 상태입니다. orderId: %d, status: %s", orderId, status)
        );
    }

    /**
     * 주문 생성 실패 - 잔고 부족
     */
    public static OrderException insufficientBalance(long currentBalance, long requiredAmount) {
        return new OrderException(
                "ORDER_004",
                String.format("잔고가 부족하여 매수 주문을 생성할 수 없습니다. 현재 잔고: %,d원, 필요 금액: %,d원", 
                        currentBalance, requiredAmount)
        );
    }

    /**
     * 주문 생성 실패 - 보유 수량 부족
     */
    public static OrderException insufficientHolding(String stockCode, int holdingQty, int requestedQty) {
        return new OrderException(
                "ORDER_005",
                String.format("보유 수량이 부족하여 매도 주문을 생성할 수 없습니다. 종목: %s, 보유: %d주, 요청: %d주",
                        stockCode, holdingQty, requestedQty)
        );
    }

    /**
     * 필수 파라미터 누락 - 지정가
     */
    public static OrderException limitPriceRequired() {
        return new OrderException(
                "ORDER_006",
                "지정가 주문은 지정가가 필요합니다"
        );
    }

    /**
     * 필수 파라미터 누락 - 예상 가격
     */
    public static OrderException estimatedPriceRequired() {
        return new OrderException(
                "ORDER_007",
                "시장가 매수 주문은 예상 가격이 필요합니다"
        );
    }

    /**
     * 이미 체결 완료된 주문
     */
    public static OrderException alreadyFilled(Long orderId) {
        return new OrderException(
                "ORDER_008",
                String.format("이미 체결 완료된 주문입니다. orderId: %d", orderId)
        );
    }
}

