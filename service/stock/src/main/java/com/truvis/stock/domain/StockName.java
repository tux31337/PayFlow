package com.truvis.stock.domain;

import com.truvis.common.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

/**
 * 종목명 값 객체
 * - 한글: "삼성전자", "SK하이닉스"
 * - 영문: "Apple Inc.", "Tesla, Inc."
 * - 1~50자 제한
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA용
@EqualsAndHashCode
@ToString
public class StockName implements ValueObject {

    @Column(name = "stock_name", nullable = false, length = 50)
    private String value;

    private StockName(String value) {
        this.value = Objects.requireNonNull(value, "종목명은 필수입니다");
        validate(value);
    }

    /**
     * 정적 팩토리 메서드
     */
    public static StockName of(String value) {
        return new StockName(value);
    }

    /**
     * 검증 로직
     */
    private void validate(String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("종목명은 공백일 수 없습니다");
        }

        if (value.length() > 50) {
            throw new IllegalArgumentException("종목명은 50자를 초과할 수 없습니다");
        }

        // 특수문자 제한 (일부 허용: 공백, 쉼표, 점, 하이픈)
        if (!value.matches("^[가-힣a-zA-Z0-9\\s,.-]+$")) {
            throw new IllegalArgumentException(
                    "종목명은 한글, 영문, 숫자, 공백, 쉼표, 점, 하이픈만 가능합니다"
            );
        }
    }

    /**
     * 한글 종목명인가?
     */
    public boolean isKorean() {
        return value.matches(".*[가-힣].*");
    }

    /**
     * 영문 종목명인가?
     */
    public boolean isEnglish() {
        return value.matches(".*[a-zA-Z].*");
    }
}