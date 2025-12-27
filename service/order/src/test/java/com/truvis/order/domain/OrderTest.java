package com.truvis.order.domain;

import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.common.model.vo.StockCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("시장가 매수 주문을 생성할 수 있다")
    void createMarketBuyOrder() {
        // given
        Long userId = 1L;
        StockCode stockCode = StockCode.of("005930"); // 삼성전자
        Quantity quantity = Quantity.of(10);

        // when
        Order order = Order.createMarketBuyOrder(userId, stockCode, quantity);

        // then
        assertNotNull(order);
        assertEquals(userId, order.getUserId());
        assertEquals(stockCode, order.getStockCode());
        assertTrue(order.isBuyOrder());
        assertTrue(order.isMarketOrder());
        assertTrue(order.isPending());
        assertEquals(quantity, order.getQuantity());
        assertEquals(Quantity.of(0), order.getFilledQuantity());
        assertEquals(quantity, order.getRemainingQuantity());
        assertNull(order.getLimitPrice());
    }

    @Test
    @DisplayName("지정가 매도 주문을 생성할 수 있다")
    void createLimitSellOrder() {
        // given
        Long userId = 1L;
        StockCode stockCode = StockCode.of("005930");
        Quantity quantity = Quantity.of(5);
        Price limitPrice = Price.of(70000L);

        // when
        Order order = Order.createLimitSellOrder(userId, stockCode, quantity, limitPrice);

        // then
        assertNotNull(order);
        assertTrue(order.isSellOrder());
        assertTrue(order.isLimitOrder());
        assertEquals(limitPrice, order.getLimitPrice());
    }

    @Test
    @DisplayName("주문을 완전히 체결할 수 있다")
    void fillOrder() {
        // given
        Order order = Order.createMarketBuyOrder(
                1L,
                StockCode.of("005930"),
                Quantity.of(10)
        );
        Quantity fillQty = Quantity.of(10);
        Price fillPrice = Price.of(70000L);

        // when
        boolean fullyFilled = order.fill(fillQty, fillPrice);

        // then
        assertTrue(fullyFilled);
        assertTrue(order.isFilled());
        assertEquals(Quantity.of(10), order.getFilledQuantity());
        assertEquals(Quantity.of(0), order.getRemainingQuantity());
        assertEquals(fillPrice, order.getAveragePrice());
    }

    @Test
    @DisplayName("주문을 부분 체결할 수 있다")
    void partiallyFillOrder() {
        // given
        Order order = Order.createMarketBuyOrder(
                1L,
                StockCode.of("005930"),
                Quantity.of(10)
        );

        // when - 첫 번째 부분 체결
        boolean fullyFilled1 = order.fill(Quantity.of(3), Price.of(70000L));

        // then
        assertFalse(fullyFilled1);
        assertTrue(order.isPartiallyFilled());
        assertEquals(Quantity.of(3), order.getFilledQuantity());
        assertEquals(Quantity.of(7), order.getRemainingQuantity());

        // when - 두 번째 부분 체결
        boolean fullyFilled2 = order.fill(Quantity.of(4), Price.of(71000L));

        // then
        assertFalse(fullyFilled2);
        assertTrue(order.isPartiallyFilled());
        assertEquals(Quantity.of(7), order.getFilledQuantity());
        assertEquals(Quantity.of(3), order.getRemainingQuantity());

        // when - 최종 체결
        boolean fullyFilled3 = order.fill(Quantity.of(3), Price.of(72000L));

        // then
        assertTrue(fullyFilled3);
        assertTrue(order.isFilled());
        assertEquals(Quantity.of(10), order.getFilledQuantity());
        assertEquals(Quantity.of(0), order.getRemainingQuantity());
        assertNotNull(order.getAveragePrice());
    }

    @Test
    @DisplayName("평균 체결가가 올바르게 계산된다")
    void calculateAveragePrice() {
        // given
        Order order = Order.createMarketBuyOrder(
                1L,
                StockCode.of("005930"),
                Quantity.of(10)
        );

        // when
        order.fill(Quantity.of(5), Price.of(70000L));  // 5주 @ 70,000원
        order.fill(Quantity.of(5), Price.of(72000L));  // 5주 @ 72,000원

        // then
        // 평균가 = (5 * 70,000 + 5 * 72,000) / 10 = 71,000원
        assertEquals(Price.of(71000L), order.getAveragePrice());
    }

    @Test
    @DisplayName("지정가 매수 주문은 현재가가 지정가 이하일 때 체결 가능하다")
    void limitBuyOrderCanFill() {
        // given
        Order order = Order.createLimitBuyOrder(
                1L,
                StockCode.of("005930"),
                Quantity.of(10),
                Price.of(70000L)
        );

        // when & then
        assertTrue(order.canFillAtPrice(Price.of(69000L))); // 지정가보다 낮음 -> 체결 가능
        assertTrue(order.canFillAtPrice(Price.of(70000L))); // 지정가와 같음 -> 체결 가능
        assertFalse(order.canFillAtPrice(Price.of(71000L))); // 지정가보다 높음 -> 체결 불가
    }

    @Test
    @DisplayName("지정가 매도 주문은 현재가가 지정가 이상일 때 체결 가능하다")
    void limitSellOrderCanFill() {
        // given
        Order order = Order.createLimitSellOrder(
                1L,
                StockCode.of("005930"),
                Quantity.of(10),
                Price.of(70000L)
        );

        // when & then
        assertFalse(order.canFillAtPrice(Price.of(69000L))); // 지정가보다 낮음 -> 체결 불가
        assertTrue(order.canFillAtPrice(Price.of(70000L)));  // 지정가와 같음 -> 체결 가능
        assertTrue(order.canFillAtPrice(Price.of(71000L)));  // 지정가보다 높음 -> 체결 가능
    }

    @Test
    @DisplayName("주문을 취소할 수 있다")
    void cancelOrder() {
        // given
        Order order = Order.createMarketBuyOrder(
                1L,
                StockCode.of("005930"),
                Quantity.of(10)
        );
        String reason = "사용자 요청";

        // when
        order.cancel(reason);

        // then
        assertTrue(order.isCancelled());
        assertTrue(order.isCompleted());
        assertEquals(reason, order.getCancellationReason());
        assertNotNull(order.getCompletedAt());
    }

    @Test
    @DisplayName("체결된 주문은 취소할 수 없다")
    void cannotCancelFilledOrder() {
        // given
        Order order = Order.createMarketBuyOrder(
                1L,
                StockCode.of("005930"),
                Quantity.of(10)
        );
        order.fill(Quantity.of(10), Price.of(70000L));

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            order.cancel("사용자 요청");
        });
    }

    @Test
    @DisplayName("체결 수량이 남은 수량을 초과할 수 없다")
    void cannotFillMoreThanRemaining() {
        // given
        Order order = Order.createMarketBuyOrder(
                1L,
                StockCode.of("005930"),
                Quantity.of(10)
        );

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            order.fill(Quantity.of(11), Price.of(70000L));
        });
    }

    @Test
    @DisplayName("주문 설명 문자열을 생성할 수 있다")
    void getDescription() {
        // given
        Order marketOrder = Order.createMarketBuyOrder(
                1L,
                StockCode.of("005930"),
                Quantity.of(10)
        );
        Order limitOrder = Order.createLimitSellOrder(
                1L,
                StockCode.of("005930"),
                Quantity.of(5),
                Price.of(70000L)
        );

        // when
        String marketDesc = marketOrder.getDescription();
        String limitDesc = limitOrder.getDescription();

        // then
        assertNotNull(marketDesc);
        assertTrue(marketDesc.contains("시장가"));
        assertTrue(marketDesc.contains("매수"));
        assertTrue(marketDesc.contains("005930"));

        assertNotNull(limitDesc);
        assertTrue(limitDesc.contains("지정가"));
        assertTrue(limitDesc.contains("매도"));
        assertTrue(limitDesc.contains("70000"));
    }
}
