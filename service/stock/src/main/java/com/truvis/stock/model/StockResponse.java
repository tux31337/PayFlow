package com.truvis.stock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockResponse {
    private Long id;
    private String stockCode;
    private String name;
    private String market;
    private String sector;
    private String currentPrice;      // "71000" or "180.50"
    private LocalDateTime priceUpdatedAt;
}