package com.truvis.transaction.domain;

import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.stock.domain.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Transaction 엔티티 테스트")
class TransactionTest {

    @Test
    @DisplayName("매수 거래 생성")
    void createBuyTransaction() {
        // given
        Long userId = 100L;
        Stock.StockCode stockCode = Stock.StockCode.of("005930");
        TransactionType type = TransactionType.BUY;
        Quantity quantity = Quantity.of(10);
        Price price = Price.of("70000");

        // when
        Transaction transaction = Transaction.create(
                userId, stockCode, type, quantity, price
        );

        // then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getUserId()).isEqualTo(100L);
        assertThat(transaction.getStockCode().getValue()).isEqualTo("005930");
        assertThat(transaction.getType()).isEqualTo(TransactionType.BUY);
        assertThat(transaction.getQuantity().getValue()).isEqualTo(10);
        assertThat(transaction.getPrice().getValue()).isEqualByComparingTo("70000.00");
        assertThat(transaction.getTotalAmount().getValue()).isEqualByComparingTo("700000.00");
        assertThat(transaction.isBuy()).isTrue();
        assertThat(transaction.isSell()).isFalse();
    }

    @Test
    @DisplayName("매도 거래 생성")
    void createSellTransaction() {
        // given
        Long userId = 100L;
        Stock.StockCode stockCode = Stock.StockCode.of("005930");
        TransactionType type = TransactionType.SELL;
        Quantity quantity = Quantity.of(5);
        Price price = Price.of("75000");

        // when
        Transaction transaction = Transaction.create(
                userId, stockCode, type, quantity, price
        );

        // then
        assertThat(transaction.getType()).isEqualTo(TransactionType.SELL);
        assertThat(transaction.isSell()).isTrue();
        assertThat(transaction.isBuy()).isFalse();
        assertThat(transaction.getTotalAmount().getValue()).isEqualByComparingTo("375000.00");
    }

    @Test
    @DisplayName("수량 변화 계산 - 매수는 +")
    void getQuantityChangeForBuy() {
        // given
        Transaction transaction = Transaction.create(
                100L,
                Stock.StockCode.of("005930"),
                TransactionType.BUY,
                Quantity.of(10),
                Price.of("70000")
        );

        // when
        int change = transaction.getQuantityChange();

        // then
        assertThat(change).isEqualTo(10);  // 매수 = +10
    }

    @Test
    @DisplayName("수량 변화 계산 - 매도는 -")
    void getQuantityChangeForSell() {
        // given
        Transaction transaction = Transaction.create(
                100L,
                Stock.StockCode.of("005930"),
                TransactionType.SELL,
                Quantity.of(5),
                Price.of("70000")
        );

        // when
        int change = transaction.getQuantityChange();

        // then
        assertThat(change).isEqualTo(-5);  // 매도 = -5
    }

    @Test
    @DisplayName("거래 설명 문자열 생성")
    void getDescription() {
        // given
        Transaction transaction = Transaction.create(
                100L,
                Stock.StockCode.of("005930"),
                TransactionType.BUY,
                Quantity.of(10),
                Price.of("70000")
        );

        // when
        String description = transaction.getDescription();

        // then
        assertThat(description).contains("매수");
        assertThat(description).contains("005930");
        assertThat(description).contains("10주");
        assertThat(description).contains("70000");
        assertThat(description).contains("700000");
    }
}
