package com.truvis.stock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockSearchResponse {
    private String stockCode;  // "005930"
    private String name;       // "삼성전자"
    private String market;     // "KOSPI"
}