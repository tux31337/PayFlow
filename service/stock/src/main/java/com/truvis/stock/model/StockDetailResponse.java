package com.truvis.stock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockDetailResponse {
    private Long id;
    private String stockCode;
    private String name;
    private String market;
    private String sector;

    // 가격 정보
    private String realtimePrice;     // "71000" (실시간)
    private String savedPrice;        // "70500" (DB 저장값)
    private String changeAmount;      // "+500" (변동금액)
    private String changeRate;        // "+0.71%" (변동률)

    private LocalDateTime priceUpdatedAt;
    private boolean isKospi;
    private boolean isKosdaq;
}