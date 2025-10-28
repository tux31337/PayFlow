package com.truvis.stock.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시장 구분 열거형
 * - 한국, 미국 증시 지원
 * - 향후 일본, 중국 등 확장 가능
 */
@Getter
@RequiredArgsConstructor
public enum Market {

    // ========== 한국 시장 ==========
    KOSPI("코스피", "유가증권시장", "KR", "대형주 중심"),
    KOSDAQ("코스닥", "코스닥시장", "KR", "중소형/IT 중심"),
    KONEX("코넥스", "중소기업전용시장", "KR", "초기 스타트업"),

    // ========== 미국 시장 ==========
    NYSE("뉴욕증권거래소", "New York Stock Exchange", "US", "세계 최대 증시"),
    NASDAQ("나스닥", "NASDAQ", "US", "기술주 중심");

    /**
     * 시장 한글/영문명 (화면 표시용)
     */
    private final String displayName;

    /**
     * 시장 정식 명칭
     */
    private final String officialName;

    /**
     * 국가 코드 (ISO 3166-1 alpha-2)
     * - KR: 한국
     * - US: 미국
     */
    private final String countryCode;

    /**
     * 시장 특징 설명
     */
    private final String description;

    // ==================== 국가별 질의 ====================

    /**
     * 한국 시장인가?
     */
    public boolean isKoreanMarket() {
        return "KR".equals(countryCode);
    }

    /**
     * 미국 시장인가?
     */
    public boolean isUSMarket() {
        return "US".equals(countryCode);
    }

    // ==================== 시장 규모별 질의 ====================

    /**
     * 대형주 시장인가?
     * - 한국: KOSPI
     * - 미국: NYSE
     */
    public boolean isLargeCap() {
        return this == KOSPI || this == NYSE;
    }

    /**
     * 중소형주/기술주 시장인가?
     * - 한국: KOSDAQ, KONEX
     * - 미국: NASDAQ
     */
    public boolean isSmallMidCap() {
        return this == KOSDAQ || this == KONEX || this == NASDAQ;
    }

    /**
     * 스타트업 전용 시장인가?
     * - 한국: KONEX
     */
    public boolean isStartupMarket() {
        return this == KONEX;
    }

    /**
     * 기술주 중심 시장인가?
     * - 한국: KOSDAQ
     * - 미국: NASDAQ
     */
    public boolean isTechMarket() {
        return this == KOSDAQ || this == NASDAQ;
    }
}