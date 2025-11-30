package com.truvis.portfolio.domain;

import com.truvis.common.model.vo.Money;
import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.common.model.vo.StockCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioTest {

    @Test
    @DisplayName("포트폴리오를 생성할 수 있다")
    void createPortfolio() {
        // given
        Long userId = 1L;
        String name = "내 포트폴리오";

        // when
        Portfolio portfolio = Portfolio.create(userId, name);

        // then
        assertNotNull(portfolio);
        assertEquals(userId, portfolio.getUserId());
        assertEquals(name, portfolio.getName());
        assertEquals(Money.of(10_000_000L), portfolio.getCashBalance());
        assertTrue(portfolio.isEmpty());
    }

    @Test
    @DisplayName("종목을 매수할 수 있다")
    void buyStock() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트");
        StockCode stockCode = StockCode.of("005930"); // 삼성전자
        Quantity quantity = Quantity.of(10);
        Price price = Price.of(70000L);

        // when
        portfolio.buyStock(stockCode, quantity, price);

        // then
        assertTrue(portfolio.hasStock(stockCode));
        assertEquals(Quantity.of(10), portfolio.getHoldingQuantity(stockCode));
        assertEquals(Money.of(9_300_000L), portfolio.getCashBalance()); // 1000만 - 70만
        assertEquals(1, portfolio.getHoldingCount());
    }

    @Test
    @DisplayName("같은 종목을 추가 매수하면 평균 단가가 계산된다")
    void buyStockMultipleTimes() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트");
        StockCode stockCode = StockCode.of("005930");

        // when
        portfolio.buyStock(stockCode, Quantity.of(10), Price.of(70000L));  // 10주 @ 70,000원
        portfolio.buyStock(stockCode, Quantity.of(10), Price.of(72000L));  // 10주 @ 72,000원

        // then
        assertEquals(Quantity.of(20), portfolio.getHoldingQuantity(stockCode));
        // 평균가: (10 * 70,000 + 10 * 72,000) / 20 = 71,000원
        assertEquals(Money.of(8_580_000L), portfolio.getCashBalance()); // 1000만 - 142만
    }

    @Test
    @DisplayName("종목을 매도할 수 있다")
    void sellStock() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트");
        StockCode stockCode = StockCode.of("005930");
        portfolio.buyStock(stockCode, Quantity.of(10), Price.of(70000L));

        // when
        portfolio.sellStock(stockCode, Quantity.of(5), Price.of(75000L)); // 5주 @ 75,000원 매도

        // then
        assertEquals(Quantity.of(5), portfolio.getHoldingQuantity(stockCode));
        assertEquals(Money.of(9_675_000L), portfolio.getCashBalance()); 
        // 930만 (매수 후) + 37.5만 (매도) = 967.5만
    }

    @Test
    @DisplayName("전량 매도하면 holding이 제거된다")
    void sellAllStock() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트");
        StockCode stockCode = StockCode.of("005930");
        portfolio.buyStock(stockCode, Quantity.of(10), Price.of(70000L));

        // when
        portfolio.sellStock(stockCode, Quantity.of(10), Price.of(75000L));

        // then
        assertFalse(portfolio.hasStock(stockCode));
        assertEquals(Quantity.of(0), portfolio.getHoldingQuantity(stockCode));
        assertTrue(portfolio.isEmpty());
    }

    @Test
    @DisplayName("잔고가 부족하면 매수할 수 없다")
    void cannotBuyWithInsufficientCash() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트", Money.of(100_000L)); // 10만원만
        StockCode stockCode = StockCode.of("005930");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.buyStock(stockCode, Quantity.of(10), Price.of(70000L)); // 70만원 필요
        });
    }

    @Test
    @DisplayName("보유하지 않은 종목은 매도할 수 없다")
    void cannotSellNonOwnedStock() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트");
        StockCode stockCode = StockCode.of("005930");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.sellStock(stockCode, Quantity.of(10), Price.of(70000L));
        });
    }

    @Test
    @DisplayName("보유 수량보다 많이 매도할 수 없다")
    void cannotSellMoreThanOwned() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트");
        StockCode stockCode = StockCode.of("005930");
        portfolio.buyStock(stockCode, Quantity.of(5), Price.of(70000L));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.sellStock(stockCode, Quantity.of(10), Price.of(70000L));
        });
    }

    @Test
    @DisplayName("최대 50개 종목까지만 보유할 수 있다")
    void cannotExceedMaxHoldings() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트");

        // 50개 종목 매수
        for (int i = 1; i <= 50; i++) {
            String code = String.format("%06d", i);
            portfolio.buyStock(StockCode.of(code), Quantity.of(1), Price.of(1000L));
        }

        // when & then - 51번째 종목 매수 시도
        assertThrows(IllegalStateException.class, () -> {
            portfolio.buyStock(StockCode.of("999999"), Quantity.of(1), Price.of(1000L));
        });
    }

    @Test
    @DisplayName("현금을 입금할 수 있다")
    void deposit() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트");
        Money depositAmount = Money.of(5_000_000L);

        // when
        portfolio.deposit(depositAmount);

        // then
        assertEquals(Money.of(15_000_000L), portfolio.getCashBalance()); // 1000만 + 500만
    }

    @Test
    @DisplayName("현금을 출금할 수 있다")
    void withdraw() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트");
        Money withdrawAmount = Money.of(1_000_000L);

        // when
        portfolio.withdraw(withdrawAmount);

        // then
        assertEquals(Money.of(9_000_000L), portfolio.getCashBalance()); // 1000만 - 100만
    }

    @Test
    @DisplayName("잔고보다 많이 출금할 수 없다")
    void cannotWithdrawMoreThanBalance() {
        // given
        Portfolio portfolio = Portfolio.create(1L, "테스트", Money.of(100_000L));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.withdraw(Money.of(200_000L));
        });
    }
}
