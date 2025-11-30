package com.truvis.order.application;

import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.common.model.vo.StockCode;
import com.truvis.order.domain.*;
import com.truvis.order.repository.OrderRepository;
import com.truvis.order.event.OrderCancelledEvent;
import com.truvis.order.event.OrderCreatedEvent;
import com.truvis.order.event.OrderFilledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Order Application Service
 * 
 * 책임:
 * 1. 주문 생성 (잔고 확인, 검증)
 * 2. 주문 조회
 * 3. 주문 취소
 * 4. 주문 체결 (Matching Engine에서 호출)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== 주문 생성 ====================

    /**
     * 시장가 매수 주문 생성
     */
    @Transactional
    public Order createMarketBuyOrder(
            Long userId,
            StockCode stockCode,
            Quantity quantity
    ) {
        log.info("시장가 매수 주문 생성 요청 - userId: {}, stockCode: {}, quantity: {}",
                userId, stockCode.getValue(), quantity.getValue());

        // TODO: 잔고 확인 로직 (나중에 Portfolio와 연동)
        // validateBalance(userId, estimatedAmount);

        // 주문 생성
        Order order = Order.createMarketBuyOrder(userId, stockCode, quantity);
        Order savedOrder = orderRepository.save(order);

        // 주문 생성 이벤트 발행
        publishOrderCreatedEvent(savedOrder);

        log.info("시장가 매수 주문 생성 완료 - orderId: {}", savedOrder.getId());
        return savedOrder;
    }

    /**
     * 지정가 매수 주문 생성
     */
    @Transactional
    public Order createLimitBuyOrder(
            Long userId,
            StockCode stockCode,
            Quantity quantity,
            Price limitPrice
    ) {
        log.info("지정가 매수 주문 생성 요청 - userId: {}, stockCode: {}, quantity: {}, limitPrice: {}",
                userId, stockCode.getValue(), quantity.getValue(), limitPrice.getValue());

        // TODO: 잔고 확인 (지정가 * 수량)
        // Money estimatedAmount = limitPrice.multiply(quantity);
        // validateBalance(userId, estimatedAmount);

        // 주문 생성
        Order order = Order.createLimitBuyOrder(userId, stockCode, quantity, limitPrice);
        Order savedOrder = orderRepository.save(order);

        // 주문 생성 이벤트 발행
        publishOrderCreatedEvent(savedOrder);

        log.info("지정가 매수 주문 생성 완료 - orderId: {}", savedOrder.getId());
        return savedOrder;
    }

    /**
     * 시장가 매도 주문 생성
     */
    @Transactional
    public Order createMarketSellOrder(
            Long userId,
            StockCode stockCode,
            Quantity quantity
    ) {
        log.info("시장가 매도 주문 생성 요청 - userId: {}, stockCode: {}, quantity: {}",
                userId, stockCode.getValue(), quantity.getValue());

        // TODO: 보유 수량 확인 (Portfolio와 연동)
        // validateHolding(userId, stockCode, quantity);

        // 주문 생성
        Order order = Order.createMarketSellOrder(userId, stockCode, quantity);
        Order savedOrder = orderRepository.save(order);

        // 주문 생성 이벤트 발행
        publishOrderCreatedEvent(savedOrder);

        log.info("시장가 매도 주문 생성 완료 - orderId: {}", savedOrder.getId());
        return savedOrder;
    }

    /**
     * 지정가 매도 주문 생성
     */
    @Transactional
    public Order createLimitSellOrder(
            Long userId,
            StockCode stockCode,
            Quantity quantity,
            Price limitPrice
    ) {
        log.info("지정가 매도 주문 생성 요청 - userId: {}, stockCode: {}, quantity: {}, limitPrice: {}",
                userId, stockCode.getValue(), quantity.getValue(), limitPrice.getValue());

        // TODO: 보유 수량 확인
        // validateHolding(userId, stockCode, quantity);

        // 주문 생성
        Order order = Order.createLimitSellOrder(userId, stockCode, quantity, limitPrice);
        Order savedOrder = orderRepository.save(order);

        // 주문 생성 이벤트 발행
        publishOrderCreatedEvent(savedOrder);

        log.info("지정가 매도 주문 생성 완료 - orderId: {}", savedOrder.getId());
        return savedOrder;
    }

    // ==================== 주문 조회 ====================

    /**
     * 주문 ID로 조회
     */
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("주문을 찾을 수 없습니다. orderId: %d", orderId)
                ));
    }

    /**
     * 사용자의 모든 주문 조회
     */
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * 사용자의 활성 주문 조회 (PENDING, PARTIALLY_FILLED)
     */
    public List<Order> getActiveOrders(Long userId) {
        return orderRepository.findActiveOrdersByUserId(userId);
    }

    /**
     * 사용자의 특정 종목 활성 주문 조회
     */
    public List<Order> getActiveOrdersByStock(Long userId, StockCode stockCode) {
        return orderRepository.findActiveOrdersByUserIdAndStockCode(userId, stockCode);
    }

    /**
     * 종목의 모든 활성 주문 조회 (Matching Engine용)
     */
    public List<Order> getActiveOrdersByStockCode(StockCode stockCode) {
        return orderRepository.findActiveOrdersByStockCode(stockCode);
    }

    /**
     * 모든 활성 주문 조회 (Matching Engine용)
     */
    public List<Order> getAllActiveOrders() {
        return orderRepository.findAllActiveOrders();
    }

    // ==================== 주문 취소 ====================

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId, String reason) {
        log.info("주문 취소 요청 - orderId: {}, userId: {}, reason: {}", orderId, userId, reason);

        Order order = getOrder(orderId);

        // 권한 확인
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 주문만 취소할 수 있습니다");
        }

        // 주문 취소
        order.cancel(reason);
        orderRepository.save(order);

        // 주문 취소 이벤트 발행
        publishOrderCancelledEvent(order, reason);

        log.info("주문 취소 완료 - orderId: {}", orderId);
    }

    // ==================== 주문 체결 (Matching Engine 호출용) ====================

    /**
     * 주문 체결
     * 
     * @param orderId 주문 ID
     * @param filledQuantity 체결 수량
     * @param filledPrice 체결 가격
     * @return 완전 체결 여부
     */
    @Transactional
    public boolean fillOrder(Long orderId, Quantity filledQuantity, Price filledPrice) {
        log.info("주문 체결 요청 - orderId: {}, filledQty: {}, filledPrice: {}",
                orderId, filledQuantity.getValue(), filledPrice.getValue());

        Order order = getOrder(orderId);

        // 주문 체결
        boolean fullyFilled = order.fill(filledQuantity, filledPrice);
        orderRepository.save(order);

        // 주문 체결 이벤트 발행
        publishOrderFilledEvent(order, filledQuantity, filledPrice, fullyFilled);

        if (fullyFilled) {
            log.info("주문 완전 체결 - orderId: {}, averagePrice: {}",
                    orderId, order.getAveragePrice().getValue());
        } else {
            log.info("주문 부분 체결 - orderId: {}, filled: {}/{}, averagePrice: {}",
                    orderId,
                    order.getFilledQuantity().getValue(),
                    order.getQuantity().getValue(),
                    order.getAveragePrice().getValue());
        }

        return fullyFilled;
    }

    /**
     * 주문 거부
     */
    @Transactional
    public void rejectOrder(Long orderId, String reason) {
        log.info("주문 거부 - orderId: {}, reason: {}", orderId, reason);

        Order order = getOrder(orderId);
        order.reject(reason);
        orderRepository.save(order);

        log.info("주문 거부 완료 - orderId: {}", orderId);
    }

    // ==================== 이벤트 발행 ====================

    private void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.of(
                order.getId(),
                order.getUserId(),
                order.getStockCode(),
                order.getSide(),
                order.getType(),
                order.getQuantity().getValue(),
                order.getLimitPrice() != null ? order.getLimitPrice().getValue().longValue() : null
        );
        eventPublisher.publishEvent(event);
        log.debug("OrderCreatedEvent 발행 - orderId: {}", order.getId());
    }

    private void publishOrderFilledEvent(
            Order order,
            Quantity filledQuantity,
            Price filledPrice,
            boolean fullyFilled
    ) {
        OrderFilledEvent event = OrderFilledEvent.of(
                order.getId(),
                order.getUserId(),
                order.getStockCode(),
                order.getSide(),
                filledQuantity.getValue(),
                filledPrice.getValue().longValue(),
                fullyFilled
        );
        eventPublisher.publishEvent(event);
        log.debug("OrderFilledEvent 발행 - orderId: {}, fullyFilled: {}", order.getId(), fullyFilled);
    }

    private void publishOrderCancelledEvent(Order order, String reason) {
        OrderCancelledEvent event = OrderCancelledEvent.of(
                order.getId(),
                order.getUserId(),
                reason
        );
        eventPublisher.publishEvent(event);
        log.debug("OrderCancelledEvent 발행 - orderId: {}", order.getId());
    }

    // ==================== 검증 로직 (TODO: Portfolio 연동 후 구현) ====================

    // private void validateBalance(Long userId, Money requiredAmount) {
    //     // Portfolio에서 잔고 확인
    //     // 잔고 부족 시 예외 발생
    // }

    // private void validateHolding(Long userId, StockCode stockCode, Quantity quantity) {
    //     // Portfolio에서 보유 수량 확인
    //     // 보유 수량 부족 시 예외 발생
    // }
}
