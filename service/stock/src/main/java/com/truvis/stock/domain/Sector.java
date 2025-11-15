package com.truvis.stock.domain;

import com.truvis.common.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 업종/섹터 값 객체
 * - 한국: "반도체", "자동차", "금융"
 * - 미국: "Technology", "Healthcare", "Finance"
 * - 1~30자 제한
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA용
@EqualsAndHashCode
@ToString
public class Sector implements ValueObject {

    @Column(name = "sector", nullable = false, length = 30)
    private String value;

    private Sector(String value) {
        this.value = Objects.requireNonNull(value, "업종은 필수입니다");
        validate(value);
    }

    /**
     * 정적 팩토리 메서드
     */
    public static Sector of(String value) {
        return new Sector(value);
    }

    /**
     * 검증 로직
     */
    private void validate(String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("업종은 공백일 수 없습니다");
        }

        if (value.length() > 30) {
            throw new IllegalArgumentException("업종은 30자를 초과할 수 없습니다");
        }

        // 한글, 영문, 숫자, 공백, 슬래시만 허용
        if (!value.matches("^[가-힣a-zA-Z0-9\\s/]+$")) {
            throw new IllegalArgumentException(
                    "업종은 한글, 영문, 숫자, 공백, 슬래시만 가능합니다"
            );
        }
    }

    // ==================== 주요 섹터 상수 (편의 메서드) ====================

    /**
     * 한국 주요 섹터
     */
    public static final class Korean {
        public static final Sector SEMICONDUCTOR = Sector.of("반도체");
        public static final Sector AUTOMOBILE = Sector.of("자동차");
        public static final Sector FINANCE = Sector.of("금융");
        public static final Sector BIO_PHARMA = Sector.of("바이오/제약");
        public static final Sector IT_SOFTWARE = Sector.of("IT/소프트웨어");
        public static final Sector STEEL = Sector.of("철강");
        public static final Sector CHEMICAL = Sector.of("화학");
        public static final Sector SHIPBUILDING = Sector.of("조선");
        public static final Sector RETAIL = Sector.of("유통");
        public static final Sector ENERGY = Sector.of("에너지");
        public static final Sector TELECOM = Sector.of("통신");
        public static final Sector CONSTRUCTION = Sector.of("건설");
        public static final Sector ENTERTAINMENT = Sector.of("엔터테인먼트");
        public static final Sector FOOD = Sector.of("식품");

        private Korean() {} // 인스턴스 생성 방지
    }

    /**
     * 미국 주요 섹터
     */
    public static final class US {
        public static final Sector TECHNOLOGY = Sector.of("Technology");
        public static final Sector HEALTHCARE = Sector.of("Healthcare");
        public static final Sector FINANCE = Sector.of("Finance");
        public static final Sector CONSUMER_CYCLICAL = Sector.of("Consumer Cyclical");
        public static final Sector CONSUMER_DEFENSIVE = Sector.of("Consumer Defensive");
        public static final Sector INDUSTRIALS = Sector.of("Industrials");
        public static final Sector ENERGY = Sector.of("Energy");
        public static final Sector UTILITIES = Sector.of("Utilities");
        public static final Sector REAL_ESTATE = Sector.of("Real Estate");
        public static final Sector COMMUNICATION = Sector.of("Communication");
        public static final Sector MATERIALS = Sector.of("Materials");

        private US() {} // 인스턴스 생성 방지
    }

    // ==================== 섹터 분류 질의 ====================

    /**
     * 기술/IT 관련 섹터인가?
     */
    public boolean isTechSector() {
        String lower = value.toLowerCase();
        return lower.contains("반도체")
                || lower.contains("it")
                || lower.contains("소프트웨어")
                || lower.contains("technology")
                || lower.contains("tech");
    }

    /**
     * 금융 관련 섹터인가?
     */
    public boolean isFinanceSector() {
        String lower = value.toLowerCase();
        return lower.contains("금융")
                || lower.contains("은행")
                || lower.contains("finance")
                || lower.contains("bank");
    }

    /**
     * 바이오/헬스케어 섹터인가?
     */
    public boolean isBioHealthSector() {
        String lower = value.toLowerCase();
        return lower.contains("바이오")
                || lower.contains("제약")
                || lower.contains("healthcare")
                || lower.contains("pharma")
                || lower.contains("bio");
    }

    /**
     * 제조업 섹터인가?
     */
    public boolean isManufacturingSector() {
        String lower = value.toLowerCase();
        return lower.contains("자동차")
                || lower.contains("철강")
                || lower.contains("화학")
                || lower.contains("조선")
                || lower.contains("industrials")
                || lower.contains("materials");
    }

    /**
     * 에너지 섹터인가?
     */
    public boolean isEnergySector() {
        String lower = value.toLowerCase();
        return lower.contains("에너지")
                || lower.contains("전력")
                || lower.contains("energy")
                || lower.contains("utilities");
    }

    /**
     * 소비재 섹터인가?
     */
    public boolean isConsumerSector() {
        String lower = value.toLowerCase();
        return lower.contains("유통")
                || lower.contains("식품")
                || lower.contains("consumer")
                || lower.contains("retail")
                || lower.contains("food");
    }

    /**
     * 한글 섹터명인가?
     */
    public boolean isKorean() {
        return value.matches(".*[가-힣].*");
    }

    /**
     * 영문 섹터명인가?
     */
    public boolean isEnglish() {
        return value.matches(".*[a-zA-Z].*") && !isKorean();
    }

    /**
     * 모든 한국 섹터 목록
     */
    public static List<Sector> getKoreanSectors() {
        return Arrays.asList(
                Korean.SEMICONDUCTOR,
                Korean.AUTOMOBILE,
                Korean.FINANCE,
                Korean.BIO_PHARMA,
                Korean.IT_SOFTWARE,
                Korean.STEEL,
                Korean.CHEMICAL,
                Korean.SHIPBUILDING,
                Korean.RETAIL,
                Korean.ENERGY,
                Korean.TELECOM,
                Korean.CONSTRUCTION,
                Korean.ENTERTAINMENT,
                Korean.FOOD
        );
    }

    /**
     * 모든 미국 섹터 목록
     */
    public static List<Sector> getUSSectors() {
        return Arrays.asList(
                US.TECHNOLOGY,
                US.HEALTHCARE,
                US.FINANCE,
                US.CONSUMER_CYCLICAL,
                US.CONSUMER_DEFENSIVE,
                US.INDUSTRIALS,
                US.ENERGY,
                US.UTILITIES,
                US.REAL_ESTATE,
                US.COMMUNICATION,
                US.MATERIALS
        );
    }
}