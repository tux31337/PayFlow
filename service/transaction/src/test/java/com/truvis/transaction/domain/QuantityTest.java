package com.truvis.transaction.domain;

import com.truvis.common.model.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Quantity 값 객체 테스트")
class QuantityTest {

    @Test
    @DisplayName("정상적인 수량으로 생성")
    void createQuantity() {
        // given
        int value = 10;

        // when
        Quantity quantity = Quantity.of(value);

        // then
        assertThat(quantity).isNotNull();
        assertThat(quantity.getValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("0 이하의 수량은 예외 발생")
    void createQuantityWithZero() {
        // when & then
        assertThatThrownBy(() -> Quantity.of(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0보다 커야 합니다");

        assertThatThrownBy(() -> Quantity.of(-10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0보다 커야 합니다");
    }

    @Test
    @DisplayName("수량 더하기")
    void addQuantity() {
        // given
        Quantity q1 = Quantity.of(10);
        Quantity q2 = Quantity.of(5);

        // when
        Quantity result = q1.add(q2);

        // then
        assertThat(result.getValue()).isEqualTo(15);
        assertThat(q1.getValue()).isEqualTo(10); // 원본 불변 확인!
    }

    @Test
    @DisplayName("수량 빼기 - 정상")
    void subtractQuantity() {
        // given
        Quantity q1 = Quantity.of(10);
        Quantity q2 = Quantity.of(3);

        // when
        Quantity result = q1.subtract(q2);

        // then
        assertThat(result.getValue()).isEqualTo(7);
    }

    @Test
    @DisplayName("수량 빼기 - 부족하면 예외")
    void subtractQuantityWithInsufficientAmount() {
        // given
        Quantity q1 = Quantity.of(5);
        Quantity q2 = Quantity.of(10);

        // when & then
        assertThatThrownBy(() -> q1.subtract(q2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량이 부족합니다");
    }

    @Test
    @DisplayName("수량 비교")
    void compareQuantity() {
        // given
        Quantity q1 = Quantity.of(10);
        Quantity q2 = Quantity.of(5);
        Quantity q3 = Quantity.of(10);

        // when & then
        assertThat(q1.isGreaterThanOrEqual(q2)).isTrue();
        assertThat(q2.isGreaterThanOrEqual(q1)).isFalse();
        assertThat(q1.isGreaterThanOrEqual(q3)).isTrue();
        assertThat(q1.isSameAs(q3)).isTrue();
    }

    @Test
    @DisplayName("같은 값이면 equals/hashCode 동일")
    void equalsAndHashCode() {
        // given
        Quantity q1 = Quantity.of(10);
        Quantity q2 = Quantity.of(10);
        Quantity q3 = Quantity.of(5);

        // when & then
        assertThat(q1).isEqualTo(q2);
        assertThat(q1).isNotEqualTo(q3);
        assertThat(q1.hashCode()).isEqualTo(q2.hashCode());
    }
}