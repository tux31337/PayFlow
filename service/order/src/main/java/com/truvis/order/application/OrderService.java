package com.truvis.order.application;

import com.truvis.common.exception.OrderException;
import com.truvis.common.model.vo.Money;
import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.common.model.vo.StockCode;
import com.truvis.order.domain.*;
import com.truvis.order.repository.OrderRepository;
import com.truvis.order.event.OrderCancelledEvent;
import com.truvis.order.event.OrderCreatedEvent;
import com.truvis.order.event.OrderFilledEvent;
import com.truvis.portfolio.application.PortfolioApplicationService;
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
    private final PortfolioApplicationService portfolioService;

    // ==================== 주문 생성 ====================

    /**
     * 시장가 매수 주문 생성
     * 
     * 참고: 시장가 주문은 정확한 체결 금액을 알 수 없으므로,
     * 예상 금액(estimatedPrice * quantity)으로 잔고 검증을 수행합니다.
     * 
     * @param userId 사용자 ID
     * @param stockCode 종목 코드
     * @param quantity 주문 수량
     * @param estimatedPrice 예상 체결가 (현재가 기준)
     */
    @Transactional
    public Order createMarketBuyOrder(
            Long userId,
            StockCode stockCode,
            Quantity quantity,
            Price estimatedPrice
    ) {
        log.info("시장가 매수 주문 생성 요청 - userId: {}, stockCode: {}, quantity: {}, estimatedPrice: {}",
                userId, stockCode.getValue(), quantity.getValue(), estimatedPrice.getValue());

        // 잔고 확인 (예상 금액 기준)
        Money estimatedAmount = estimatedPrice.multiply(quantity);
        portfolioService.validateBuyOrder(userId, estimatedAmount);

        // 주문 생성
        Order order = Order.createMarketBuyOrder(userId, stockCode, quantity);
        Order savedOrder = orderRepository.save(order);

        // 주문 생성 이벤트 발행
        publishOrderCreatedEvent(savedOrder);

        log.info("시장가 매수 주문 생성 완료 - orderId: {}", savedOrder.getId());
        return savedOrder;
    }

    /**
     * 시장가 매수 주문 생성 (잔고 검증 없이 - 기존 호환성)
     * 
     * @deprecated 잔고 검증이 포함된 메서드 사용 권장
     */
    @Transactional
    public Order createMarketBuyOrder(
            Long userId,
            StockCode stockCode,
            Quantity quantity
    ) {
        log.info("시장가 매수 주문 생성 요청 (검증 생략) - userId: {}, stockCode: {}, quantity: {}",
                userId, stockCode.getValue(), quantity.getValue());

        // 주문 생성 (검증 생략)
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

        // 잔고 확인 (지정가 * 수량)
        Money requiredAmount = limitPrice.multiply(quantity);
        portfolioService.validateBuyOrder(userId, requiredAmount);

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

        // 보유 수량 확인
        portfolioService.validateSellOrder(userId, stockCode, quantity);

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

        // 보유 수량 확인
        portfolioService.validateSellOrder(userId, stockCode, quantity);

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
                .orElseThrow(() -> OrderException.notFound(orderId));
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
            throw OrderException.unauthorized(orderId);
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

    // ==================== 검증 헬퍼 메서드 ====================

    /**
     * 매수 가능 여부 확인 (조회용)
     */
    public boolean canBuy(Long userId, Money amount) {
        return portfolioService.canBuy(userId, amount);
    }

    /**
     * 매도 가능 여부 확인 (조회용)
     */
    public boolean canSell(Long userId, StockCode stockCode, Quantity quantity) {
        return portfolioService.canSell(userId, stockCode, quantity);
    }

    /**
     * 현금 잔고 조회
     */
    public Money getCashBalance(Long userId) {
        return portfolioService.getCashBalance(userId);
    }

    /**
     * 특정 종목 보유 수량 조회
     */
    public Quantity getHoldingQuantity(Long userId, StockCode stockCode) {
        return portfolioService.getHoldingQuantity(userId, stockCode);
    }
}
