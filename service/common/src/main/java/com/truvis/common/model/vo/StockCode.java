package com.truvis.common.model.vo;

import com.truvis.common.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

/**
 * 종목 코드 값 객체
 * - 한국: 6자리 숫자 (예: "005930" - 삼성전자)
 * - 미국: 영문 (예: "AAPL" - 애플)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA용
@EqualsAndHashCode
@ToString
public class StockCode implements ValueObject {

    @Column(name = "stock_code", nullable = false, length = 20)
    private String value;

    private StockCode(String value) {
        this.value = Objects.requireNonNull(value, "종목 코드는 필수입니다");
        validate(value);
    }

    /**
     * 정적 팩토리 메서드
     */
    public static StockCode of(String value) {
        return new StockCode(value);
    }

    /**
     * 검증 로직
     */
    private void validate(String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("종목 코드는 공백일 수 없습니다");
        }

        if (value.length() > 20) {
            throw new IllegalArgumentException("종목 코드는 20자를 초과할 수 없습니다");
        }
    }
}