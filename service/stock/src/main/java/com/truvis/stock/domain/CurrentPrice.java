package com.truvis.stock.domain;

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
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * 현재가 값 객체
 * - 주식의 현재 거래 가격
 * - BigDecimal 사용 (정확한 금액 계산)
 * - 양수만 허용 (가격은 항상 > 0)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA용
@EqualsAndHashCode
@ToString
public class CurrentPrice implements ValueObject {

    private static final int SCALE = 2;  // 소수점 2자리
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");  // 최소 1원 (또는 1센트)

    @Column(name = "current_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    private CurrentPrice(BigDecimal value) {
        validate(value);
        this.value = value.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 정적 팩토리 메서드 - BigDecimal
     */
    public static CurrentPrice of(BigDecimal value) {
        return new CurrentPrice(value);
    }

    /**
     * 정적 팩토리 메서드 - long (한국 주식 - 원 단위)
     */
    public static CurrentPrice of(long value) {
        return new CurrentPrice(BigDecimal.valueOf(value));
    }

    /**
     * 정적 팩토리 메서드 - double (미국 주식 - 달러 단위)
     */
    public static CurrentPrice of(double value) {
        return new CurrentPrice(BigDecimal.valueOf(value));
    }

    /**
     * 정적 팩토리 메서드 - String
     */
    public static CurrentPrice of(String value) {
        return new CurrentPrice(new BigDecimal(value));
    }

    /**
     * 검증 로직
     */
    private void validate(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("현재가는 필수입니다");
        }

        // 가격은 반드시 양수 (0원짜리 주식은 없음!)
        if (value.compareTo(MIN_PRICE) < 0) {
            throw new IllegalArgumentException(
                    String.format("현재가는 %s 이상이어야 합니다 (입력값: %s)",
                            MIN_PRICE, value)
            );
        }

        // 너무 큰 값 방지 (1조 원 이상은 비현실적)
        BigDecimal maxPrice = new BigDecimal("1000000000000");  // 1조
        if (value.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    String.format("현재가가 너무 큽니다 (최대: %s)", maxPrice)
            );
        }
    }

    // ==================== 가격 연산 ====================

    /**
     * 가격 더하기
     */
    public CurrentPrice add(CurrentPrice other) {
        return new CurrentPrice(this.value.add(other.value));
    }

    /**
     * 가격 빼기
     */
    public CurrentPrice subtract(CurrentPrice other) {
        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(MIN_PRICE) < 0) {
            throw new IllegalArgumentException("가격은 음수가 될 수 없습니다");
        }
        return new CurrentPrice(result);
    }

    /**
     * 곱하기 (예: 수량 계산)
     */
    public BigDecimal multiply(long quantity) {
        return this.value.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * 퍼센트 적용 (예: +5% 상승)
     */
    public CurrentPrice applyPercentage(double percentage) {
        BigDecimal multiplier = BigDecimal.ONE.add(
                BigDecimal.valueOf(percentage / 100.0)
        );
        return new CurrentPrice(this.value.multiply(multiplier));
    }

    // ==================== 가격 비교 ====================

    /**
     * 더 비싼가?
     */
    public boolean isGreaterThan(CurrentPrice other) {
        return this.value.compareTo(other.value) > 0;
    }

    /**
     * 더 싼가?
     */
    public boolean isLessThan(CurrentPrice other) {
        return this.value.compareTo(other.value) < 0;
    }

    /**
     * 같은가?
     */
    public boolean isSameAs(CurrentPrice other) {
        return this.value.compareTo(other.value) == 0;
    }

    // ==================== 변동률 계산 ====================

    /**
     * 가격 변동률 계산 (%)
     * - (현재가 - 이전가) / 이전가 × 100
     *
     * @param previousPrice 이전 가격
     * @return 변동률 (예: 5.5는 +5.5%, -3.2는 -3.2%)
     */
    public double calculateChangeRate(CurrentPrice previousPrice) {
        Objects.requireNonNull(previousPrice, "이전 가격은 필수입니다");

        BigDecimal change = this.value.subtract(previousPrice.value);
        BigDecimal rate = change
                .divide(previousPrice.value, 4, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100));

        return rate.doubleValue();
    }

    /**
     * 가격 변동 금액 계산
     * - 현재가 - 이전가
     */
    public BigDecimal calculateChangeAmount(CurrentPrice previousPrice) {
        Objects.requireNonNull(previousPrice, "이전 가격은 필수입니다");
        return this.value.subtract(previousPrice.value);
    }

    /**
     * 상승했는가?
     */
    public boolean isUp(CurrentPrice previousPrice) {
        return calculateChangeAmount(previousPrice).compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 하락했는가?
     */
    public boolean isDown(CurrentPrice previousPrice) {
        return calculateChangeAmount(previousPrice).compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 보합인가? (변동 없음)
     */
    public boolean isFlat(CurrentPrice previousPrice) {
        return calculateChangeAmount(previousPrice).compareTo(BigDecimal.ZERO) == 0;
    }

    // ==================== 포맷팅 ====================

    /**
     * 천 단위 구분자 포맷 (한국)
     * - 예: 71000 → "71,000"
     */
    public String formatKorean() {
        DecimalFormat formatter = new DecimalFormat("#,##0");
        return formatter.format(value);
    }

    /**
     * 달러 포맷 (미국)
     * - 예: 180.50 → "$180.50"
     */
    public String formatUS() {
        DecimalFormat formatter = new DecimalFormat("$#,##0.00");
        return formatter.format(value);
    }

    /**
     * 변동률 포맷
     * - 예: 5.5 → "+5.50%", -3.2 → "-3.20%"
     */
    public String formatChangeRate(CurrentPrice previousPrice) {
        double rate = calculateChangeRate(previousPrice);
        String sign = rate >= 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, rate);
    }

    // ==================== 가격대 분류 ====================

    /**
     * 저가주인가? (1만원 미만)
     */
    public boolean isLowPrice() {
        return value.compareTo(new BigDecimal("10000")) < 0;
    }

    /**
     * 중가주인가? (1만원~10만원)
     */
    public boolean isMidPrice() {
        BigDecimal ten = new BigDecimal("10000");
        BigDecimal hundred = new BigDecimal("100000");
        return value.compareTo(ten) >= 0 && value.compareTo(hundred) < 0;
    }

    /**
     * 고가주인가? (10만원 이상)
     */
    public boolean isHighPrice() {
        return value.compareTo(new BigDecimal("100000")) >= 0;
    }
}