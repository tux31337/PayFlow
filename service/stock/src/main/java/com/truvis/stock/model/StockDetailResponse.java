package com.truvis.stock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockDetailResponse {
    // 기본 정보
    private Long id;
    private String stockCode;
    private String name;
    private String market;
    private String sector;

    // 가격 정보
    private String currentPrice;      // "71,000" (현재 실시간 가격, 포맷팅)
    private String previousPrice;     // "70,500" (이전 저장 가격, 포맷팅)
    private String changeAmount;      // "+500" (변동금액)
    private String changeRate;        // "+0.71%" (변동률)

    // 메타 정보
    private LocalDateTime priceUpdatedAt;
    private LocalDateTime createdAt;
}