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
    private String currentPrice;
    private LocalDateTime priceUpdatedAt;
    private LocalDateTime createdAt;
}