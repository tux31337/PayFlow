package com.truvis.common.model.vo;

import com.truvis.common.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * 수량 값 객체
 * - 주식 거래 수량
 * - 반드시 양수
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@ToString
public class Quantity implements ValueObject {

    @Column(name = "quantity", nullable = false)
    private int value;

    private Quantity(int value) {
        validate(value);
        this.value = value;
    }

    /**
     * 정적 팩토리 메서드
     */
    public static Quantity of(int value) {
        return new Quantity(value);
    }

    /**
     * 검증 로직
     */
    private void validate(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다");
        }
    }

    /**
     * 수량 더하기 (매수 시 사용)
     */
    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    /**
     * 수량 빼기 (매도 시 사용)
     */
    public Quantity subtract(Quantity other) {
        int newValue = this.value - other.value;
        if (newValue < 0) {
            throw new IllegalArgumentException(
                    String.format("수량이 부족합니다. 현재: %d, 요청: %d", this.value, other.value)
            );
        }
        return new Quantity(newValue);
    }

    /**
     * 비교: 충분한 수량이 있는가?
     */
    public boolean isGreaterThanOrEqual(Quantity other) {
        return this.value >= other.value;
    }

    /**
     * 비교: 같은 수량인가?
     */
    public boolean isSameAs(Quantity other) {
        return this.value == other.value;
    }
}