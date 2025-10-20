package com.truvis.transaction.domain;

import com.truvis.common.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 단가 값 객체
 * - 주식 1주당 가격
 * - BigDecimal 사용 (정확한 금액 계산)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@ToString
public class Price implements ValueObject {

    private static final int SCALE = 2;  // 소수점 2자리
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;  // 반올림

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    private Price(BigDecimal value) {
        validate(value);
        this.value = value.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 정적 팩토리 메서드 - BigDecimal
     */
    public static Price of(BigDecimal value) {
        return new Price(value);
    }

    /**
     * 정적 팩토리 메서드 - String (추천!)
     */
    public static Price of(String value) {
        return new Price(new BigDecimal(value));
    }

    /**
     * 정적 팩토리 메서드 - long (원화용)
     */
    public static Price of(long value) {
        return new Price(BigDecimal.valueOf(value));
    }

    /**
     * 검증 로직
     */
    private void validate(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("가격은 필수입니다");
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다");
        }
    }

    /**
     * 수량과 곱해서 총액 계산
     */
    public Money multiply(Quantity quantity) {
        BigDecimal total = this.value.multiply(
                BigDecimal.valueOf(quantity.getValue())
        );
        return Money.of(total);
    }

    /**
     * 가격 비교: 더 비싼가?
     */
    public boolean isGreaterThan(Price other) {
        return this.value.compareTo(other.value) > 0;
    }

    /**
     * 가격 비교: 더 싼가?
     */
    public boolean isLessThan(Price other) {
        return this.value.compareTo(other.value) < 0;
    }

    /**
     * 가격 비교: 같은가?
     */
    public boolean isSameAs(Price other) {
        return this.value.compareTo(other.value) == 0;
    }
}