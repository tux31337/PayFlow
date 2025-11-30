package com.truvis.order.application;

import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.common.model.vo.StockCode;
import com.truvis.order.domain.Order;
import com.truvis.order.domain.OrderStatus;
import com.truvis.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderService orderService;
    private OrderRepository orderRepository;
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        orderService = new OrderService(orderRepository, eventPublisher);
    }

    private Order setOrderId(Order order, Long id) {
        try {
            java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, id);
            return order;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("시장가 매수 주문을 생성할 수 있다")
    void createMarketBuyOrder() {
        // given
        Long userId = 1L;
        StockCode stockCode = StockCode.of("005930");
        Quantity quantity = Quantity.of(10);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return setOrderId(order, 1L);
        });

        // when
        Order createdOrder = orderService.createMarketBuyOrder(userId, stockCode, quantity);

        // then
        assertNotNull(createdOrder);
        assertTrue(createdOrder.isBuyOrder());
        assertTrue(createdOrder.isMarketOrder());
        assertTrue(createdOrder.isPending());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("지정가 매도 주문을 생성할 수 있다")
    void createLimitSellOrder() {
        // given
        Long userId = 1L;
        StockCode stockCode = StockCode.of("005930");
        Quantity quantity = Quantity.of(5);
        Price limitPrice = Price.of(70000L);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return setOrderId(order, 1L);
        });

        // when
        Order createdOrder = orderService.createLimitSellOrder(userId, stockCode, quantity, limitPrice);

        // then
        assertNotNull(createdOrder);
        assertTrue(createdOrder.isSellOrder());
        assertTrue(createdOrder.isLimitOrder());
        assertEquals(limitPrice, createdOrder.getLimitPrice());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("사용자의 활성 주문을 조회할 수 있다")
    void getActiveOrders() {
        // given
        Long userId = 1L;
        Order order1 = setOrderId(Order.createMarketBuyOrder(userId, StockCode.of("005930"), Quantity.of(10)), 1L);
        Order order2 = setOrderId(Order.createLimitBuyOrder(userId, StockCode.of("035420"), Quantity.of(5), Price.of(100000L)), 2L);
        
        when(orderRepository.findActiveOrdersByUserId(userId))
                .thenReturn(Arrays.asList(order1, order2));

        // when
        List<Order> activeOrders = orderService.getActiveOrders(userId);

        // then
        assertEquals(2, activeOrders.size());
        assertTrue(activeOrders.stream().allMatch(Order::isActive));
        verify(orderRepository).findActiveOrdersByUserId(userId);
    }

    @Test
    @DisplayName("주문을 취소할 수 있다")
    void cancelOrder() {
        // given
        Long orderId = 1L;
        Long userId = 1L;
        String reason = "사용자 요청";

        Order order = setOrderId(Order.createMarketBuyOrder(userId, StockCode.of("005930"), Quantity.of(10)), orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        orderService.cancelOrder(orderId, userId, reason);

        // then
        assertTrue(order.isCancelled());
        assertEquals(reason, order.getCancellationReason());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("다른 사용자의 주문은 취소할 수 없다")
    void cannotCancelOthersOrder() {
        // given
        Long orderId = 1L;
        Long orderOwnerId = 1L;
        Long otherUserId = 2L;

        Order order = setOrderId(Order.createMarketBuyOrder(orderOwnerId, StockCode.of("005930"), Quantity.of(10)), orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.cancelOrder(orderId, otherUserId, "사용자 요청");
        });
    }

    @Test
    @DisplayName("주문을 체결할 수 있다")
    void fillOrder() {
        // given
        Long orderId = 1L;
        Order order = setOrderId(Order.createMarketBuyOrder(1L, StockCode.of("005930"), Quantity.of(10)), orderId);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        boolean fullyFilled = orderService.fillOrder(
                orderId,
                Quantity.of(10),
                Price.of(70000L)
        );

        // then
        assertTrue(fullyFilled);
        assertTrue(order.isFilled());
        assertEquals(Quantity.of(10), order.getFilledQuantity());
        assertEquals(Price.of(70000L), order.getAveragePrice());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("주문을 부분 체결할 수 있다")
    void partiallyFillOrder() {
        // given
        Long orderId = 1L;
        Order order = setOrderId(Order.createMarketBuyOrder(1L, StockCode.of("005930"), Quantity.of(10)), orderId);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when - 부분 체결
        boolean fullyFilled = orderService.fillOrder(
                orderId,
                Quantity.of(5),
                Price.of(70000L)
        );

        // then
        assertFalse(fullyFilled);
        assertTrue(order.isPartiallyFilled());
        assertEquals(Quantity.of(5), order.getFilledQuantity());
        assertEquals(Quantity.of(5), order.getRemainingQuantity());
    }

    @Test
    @DisplayName("주문을 거부할 수 있다")
    void rejectOrder() {
        // given
        Long orderId = 1L;
        String reason = "잔고 부족";
        Order order = setOrderId(Order.createMarketBuyOrder(1L, StockCode.of("005930"), Quantity.of(10)), orderId);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        orderService.rejectOrder(orderId, reason);

        // then
        assertEquals(OrderStatus.REJECTED, order.getStatus());
        assertEquals(reason, order.getCancellationReason());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 시 예외가 발생한다")
    void getOrderNotFound() {
        // given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrder(orderId);
        });
    }
}
