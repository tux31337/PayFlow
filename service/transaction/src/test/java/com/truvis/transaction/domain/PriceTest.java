package com.truvis.transaction.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Price 값 객체 테스트")
class PriceTest {

    @Test
    @DisplayName("String으로 가격 생성")
    void createPriceFromString() {
        // when
        Price price = Price.of("70000");

        // then
        assertThat(price).isNotNull();
        assertThat(price.getValue()).isEqualByComparingTo(new BigDecimal("70000.00"));
    }

    @Test
    @DisplayName("long으로 가격 생성")
    void createPriceFromLong() {
        // when
        Price price = Price.of(70000L);

        // then
        assertThat(price.getValue()).isEqualByComparingTo(new BigDecimal("70000.00"));
    }

    @Test
    @DisplayName("0 이하의 가격은 예외")
    void createPriceWithZero() {
        // when & then
        assertThatThrownBy(() -> Price.of("0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0보다 커야 합니다");
    }

    @Test
    @DisplayName("가격 × 수량 = 총액")
    void multiplyByQuantity() {
        // given
        Price price = Price.of("70000");
        Quantity quantity = Quantity.of(10);

        // when
        Money totalAmount = price.multiply(quantity);

        // then
        assertThat(totalAmount.getValue()).isEqualByComparingTo(new BigDecimal("700000.00"));
    }

    @Test
    @DisplayName("가격 비교")
    void comparePrice() {
        // given
        Price p1 = Price.of("70000");
        Price p2 = Price.of("80000");
        Price p3 = Price.of("70000");

        // when & then
        assertThat(p1.isGreaterThan(p2)).isFalse();
        assertThat(p2.isGreaterThan(p1)).isTrue();
        assertThat(p1.isLessThan(p2)).isTrue();
        assertThat(p1.isSameAs(p3)).isTrue();
    }
}