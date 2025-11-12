package com.truvis.stock.domain.event;

import com.truvis.common.model.vo.StockCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주식 가격 변경 도메인 이벤트
 * - KIS WebSocket에서 실시간 체결가 수신 시 발행
 * - SSE를 통해 클라이언트에게 전달
 */
@Getter
@Builder
@AllArgsConstructor
public class StockPriceUpdateEvent {
    
    /**
     * 종목 코드
     */
    private final StockCode stockCode;
    
    /**
     * 현재가
     */
    private final long currentPrice;
    
    /**
     * 전일 대비
     */
    private final long priceChange;
    
    /**
     * 등락률 (%)
     */
    private final double changeRate;
    
    /**
     * 체결 시각
     */
    private final LocalDateTime tradeTime;
    
    /**
     * 체결량
     */
    private final long volume;
    
    /**
     * 이벤트 발생 시각
     */
    private final LocalDateTime eventTime;
    
    public static StockPriceUpdateEvent of(
            StockCode stockCode,
            long currentPrice,
            long priceChange,
            double changeRate,
            LocalDateTime tradeTime,
            long volume
    ) {
        return StockPriceUpdateEvent.builder()
                .stockCode(stockCode)
                .currentPrice(currentPrice)
                .priceChange(priceChange)
                .changeRate(changeRate)
                .tradeTime(tradeTime)
                .volume(volume)
                .eventTime(LocalDateTime.now())
                .build();
    }
}
