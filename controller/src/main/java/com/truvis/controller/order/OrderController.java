package com.truvis.controller.order;

import com.truvis.common.exception.OrderException;
import com.truvis.common.model.vo.Money;
import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.common.model.vo.StockCode;
import com.truvis.common.security.SecurityUtils;
import com.truvis.order.application.OrderService;
import com.truvis.order.domain.Order;
import com.truvis.order.domain.OrderSide;
import com.truvis.order.domain.OrderStatus;
import com.truvis.order.domain.OrderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ==================== 주문 생성 ====================

    /**
     * 매수 주문 생성
     * POST /api/orders/buy
     */
    @PostMapping("/buy")
    public ResponseEntity<OrderResponse> createBuyOrder(
            @RequestBody CreateOrderRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("[Order] 매수 주문 생성 - userId: {}, stockCode: {}, type: {}, quantity: {}",
                userId, request.getStockCode(), request.getOrderType(), request.getQuantity());

        Order order;
        if (request.getOrderType() == OrderType.MARKET) {
            // 시장가 매수
            if (request.getEstimatedPrice() == null) {
                throw OrderException.estimatedPriceRequired();
            }
            order = orderService.createMarketBuyOrder(
                    userId,
                    StockCode.of(request.getStockCode()),
                    Quantity.of(request.getQuantity()),
                    Price.of(request.getEstimatedPrice())
            );
        } else {
            // 지정가 매수
            if (request.getLimitPrice() == null) {
                throw OrderException.limitPriceRequired();
            }
            order = orderService.createLimitBuyOrder(
                    userId,
                    StockCode.of(request.getStockCode()),
                    Quantity.of(request.getQuantity()),
                    Price.of(request.getLimitPrice())
            );
        }

        return ResponseEntity.ok(OrderResponse.from(order));
    }

    /**
     * 매도 주문 생성
     * POST /api/orders/sell
     */
    @PostMapping("/sell")
    public ResponseEntity<OrderResponse> createSellOrder(
            @RequestBody CreateOrderRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("[Order] 매도 주문 생성 - userId: {}, stockCode: {}, type: {}, quantity: {}",
                userId, request.getStockCode(), request.getOrderType(), request.getQuantity());

        Order order;
        if (request.getOrderType() == OrderType.MARKET) {
            // 시장가 매도
            order = orderService.createMarketSellOrder(
                    userId,
                    StockCode.of(request.getStockCode()),
                    Quantity.of(request.getQuantity())
            );
        } else {
            // 지정가 매도
            if (request.getLimitPrice() == null) {
                throw OrderException.limitPriceRequired();
            }
            order = orderService.createLimitSellOrder(
                    userId,
                    StockCode.of(request.getStockCode()),
                    Quantity.of(request.getQuantity()),
                    Price.of(request.getLimitPrice())
            );
        }

        return ResponseEntity.ok(OrderResponse.from(order));
    }

    // ==================== 주문 조회 ====================

    /**
     * 주문 상세 조회
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId
    ) {
        Order order = orderService.getOrder(orderId);
        
        // 본인 주문인지 확인
        Long userId = SecurityUtils.getCurrentUserId();
        if (!order.getUserId().equals(userId)) {
            throw OrderException.unauthorized(orderId);
        }
        
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    /**
     * 내 주문 목록 조회
     * GET /api/orders/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Order> orders = orderService.getUserOrders(userId);
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 내 활성 주문 목록 조회 (PENDING, PARTIALLY_FILLED)
     * GET /api/orders/me/active
     */
    @GetMapping("/me/active")
    public ResponseEntity<List<OrderResponse>> getMyActiveOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Order> orders = orderService.getActiveOrders(userId);
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // ==================== 주문 취소 ====================

    /**
     * 주문 취소
     * DELETE /api/orders/{orderId}
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "사용자 요청") String reason
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("[Order] 주문 취소 - orderId: {}, userId: {}, reason: {}", orderId, userId, reason);

        orderService.cancelOrder(orderId, userId, reason);
        return ResponseEntity.noContent().build();
    }

    // ==================== 잔고/보유량 확인 ====================

    /**
     * 매수 가능 금액 확인
     * GET /api/orders/check/buy
     */
    @GetMapping("/check/buy")
    public ResponseEntity<BuyCheckResponse> checkBuyAvailability(
            @RequestParam long amount
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        Money money = Money.of(amount);
        boolean canBuy = orderService.canBuy(userId, money);
        Money balance = orderService.getCashBalance(userId);

        return ResponseEntity.ok(new BuyCheckResponse(
                canBuy,
                balance.getValue(),
                BigDecimal.valueOf(amount)
        ));
    }

    /**
     * 매도 가능 수량 확인
     * GET /api/orders/check/sell
     */
    @GetMapping("/check/sell")
    public ResponseEntity<SellCheckResponse> checkSellAvailability(
            @RequestParam String stockCode,
            @RequestParam int quantity
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        StockCode code = StockCode.of(stockCode);
        Quantity qty = Quantity.of(quantity);
        boolean canSell = orderService.canSell(userId, code, qty);
        Quantity holdingQty = orderService.getHoldingQuantity(userId, code);

        return ResponseEntity.ok(new SellCheckResponse(
                canSell,
                holdingQty.getValue(),
                quantity
        ));
    }

    // ==================== Request/Response DTOs ====================

    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateOrderRequest {
        private String stockCode;
        private OrderType orderType;  // MARKET or LIMIT
        private int quantity;
        private Long limitPrice;      // 지정가 (LIMIT 주문 시 필수)
        private Long estimatedPrice;  // 예상 가격 (MARKET 매수 시 필수)
    }

    @lombok.Getter
    @lombok.Builder
    public static class OrderResponse {
        private Long orderId;
        private Long userId;
        private String stockCode;
        private OrderSide side;
        private OrderType type;
        private OrderStatus status;
        private int quantity;
        private int filledQuantity;
        private int remainingQuantity;
        private BigDecimal limitPrice;
        private BigDecimal averagePrice;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static OrderResponse from(Order order) {
            return OrderResponse.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .stockCode(order.getStockCode().getValue())
                    .side(order.getSide())
                    .type(order.getType())
                    .status(order.getStatus())
                    .quantity(order.getQuantity().getValue())
                    .filledQuantity(order.getFilledQuantity().getValue())
                    .remainingQuantity(order.getRemainingQuantity().getValue())
                    .limitPrice(order.getLimitPrice() != null ? order.getLimitPrice().getValue() : null)
                    .averagePrice(order.getAveragePrice() != null ? order.getAveragePrice().getValue() : null)
                    .totalAmount(order.getTotalFilledAmount() != null ? order.getTotalFilledAmount().getValue() : null)
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .build();
        }
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class BuyCheckResponse {
        private boolean canBuy;
        private BigDecimal currentBalance;
        private BigDecimal requiredAmount;
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class SellCheckResponse {
        private boolean canSell;
        private int holdingQuantity;
        private int requestedQuantity;
    }
}
