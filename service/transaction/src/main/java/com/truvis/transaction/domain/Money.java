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
 * 금액 값 객체
 * - 거래 총액, 포트폴리오 가치 등
 * - BigDecimal 사용 (정확한 금액 계산)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@ToString
public class Money implements ValueObject {

    private static final int SCALE = 2;  // 소수점 2자리
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    private Money(BigDecimal value) {
        validate(value);
        this.value = value.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 정적 팩토리 메서드 - BigDecimal
     */
    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    /**
     * 정적 팩토리 메서드 - String
     */
    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }

    /**
     * 정적 팩토리 메서드 - long
     */
    public static Money of(long value) {
        return new Money(BigDecimal.valueOf(value));
    }

    /**
     * 0원 생성
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    /**
     * 검증 로직
     */
    private void validate(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("금액은 필수입니다");
        }

        // Money는 0원도 허용 (Price와 다른 점!)
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 음수일 수 없습니다");
        }
    }

    /**
     * 금액 더하기
     */
    public Money add(Money other) {
        return new Money(this.value.add(other.value));
    }

    /**
     * 금액 빼기
     */
    public Money subtract(Money other) {
        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    String.format("금액이 부족합니다. 현재: %s, 요청: %s",
                            this.value, other.value)
            );
        }
        return new Money(result);
    }

    /**
     * 곱하기 (배수 계산)
     */
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.value.multiply(multiplier));
    }

    /**
     * 나누기 (평균 계산 등)
     */
    public Money divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("0으로 나눌 수 없습니다");
        }
        return new Money(this.value.divide(divisor, SCALE, ROUNDING_MODE));
    }

    /**
     * 비교: 더 큰가?
     */
    public boolean isGreaterThan(Money other) {
        return this.value.compareTo(other.value) > 0;
    }

    /**
     * 비교: 더 작은가?
     */
    public boolean isLessThan(Money other) {
        return this.value.compareTo(other.value) < 0;
    }

    /**
     * 비교: 크거나 같은가?
     */
    public boolean isGreaterThanOrEqual(Money other) {
        return this.value.compareTo(other.value) >= 0;
    }

    /**
     * 비교: 0원인가?
     */
    public boolean isZero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 비교: 양수인가?
     */
    public boolean isPositive() {
        return this.value.compareTo(BigDecimal.ZERO) > 0;
    }
}