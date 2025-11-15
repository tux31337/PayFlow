package com.truvis.stock.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 실시간 주가 변경 응답
 */
@Getter
@Builder
@AllArgsConstructor
public class StockPriceUpdateResponse {
    
    /**
     * 종목코드
     */
    private String stockCode;
    
    /**
     * 현재가
     */
    private long currentPrice;
    
    /**
     * 전일대비
     */
    private long priceChange;
    
    /**
     * 등락률 (%)
     */
    private double changeRate;
    
    /**
     * 체결 시각
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeTime;
    
    /**
     * 체결량
     */
    private long volume;
}
