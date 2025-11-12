package com.truvis.stock.infrastructure.kis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KIS 실시간 체결가 데이터
 * - WebSocket으로 수신한 파이프(|) 구분 데이터를 파싱
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KisRealtimePriceData {
    
    /**
     * 유통구분코드 (MKSC_SHRN_ISCD)
     */
    private String marketCode;
    
    /**
     * 종목코드 (STCK_SHRN_ISCD)
     */
    private String stockCode;
    
    /**
     * 체결시각 (STCK_CNTG_HOUR) - HHMMSS
     */
    private String tradeTime;
    
    /**
     * 현재가 (STCK_PRPR)
     */
    private long currentPrice;
    
    /**
     * 전일대비부호 (PRDY_VRSS_SIGN)
     * 1: 상한, 2: 상승, 3: 보합, 4: 하한, 5: 하락
     */
    private String priceChangeSign;
    
    /**
     * 전일대비 (PRDY_VRSS)
     */
    private long priceChange;
    
    /**
     * 전일대비율 (PRDY_CTRT)
     */
    private double changeRate;
    
    /**
     * 가중평균가격 (WGHN_AVRG_STCK_PRC)
     */
    private long weightedAvgPrice;
    
    /**
     * 시가 (STCK_OPRC)
     */
    private long openPrice;
    
    /**
     * 고가 (STCK_HGPR)
     */
    private long highPrice;
    
    /**
     * 저가 (STCK_LWPR)
     */
    private long lowPrice;
    
    /**
     * 체결거래량 (CNTG_VOL)
     */
    private long tradeVolume;
    
    /**
     * 누적거래량 (ACML_VOL)
     */
    private long accumulatedVolume;
    
    /**
     * 누적거래대금 (ACML_TR_PBMN)
     */
    private long accumulatedAmount;
    
    /**
     * 파이프 구분 문자열에서 파싱
     * 
     * 형식: "0|H0STCNT0|001|005930^095544^102400^5^-1100^-1.06^..."
     * - 앞 3개 필드는 헤더 (응답코드|TR_ID|연속구분)
     * - 4번째 필드부터 실제 데이터 (^ 구분)
     */
    public static KisRealtimePriceData parse(String data) {
        // 1. 파이프로 분리
        String[] parts = data.split("\\|");
        
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid realtime price data format: " + data);
        }
        
        // 2. 실제 데이터는 4번째 필드 (^ 구분)
        String[] fields = parts[3].split("\\^");
        
        if (fields.length < 15) {
            throw new IllegalArgumentException("Invalid realtime price fields: " + parts[3]);
        }
        
        return KisRealtimePriceData.builder()
                .marketCode(parts[2])           // 001 (코스피)
                .stockCode(fields[0])           // 종목코드
                .tradeTime(fields[1])           // 체결시각 HHMMSS
                .currentPrice(parseLong(fields[2]))      // 현재가
                .priceChangeSign(fields[3])     // 전일대비부호
                .priceChange(parseLong(fields[4]))       // 전일대비
                .changeRate(parseDouble(fields[5]))      // 전일대비율
                .weightedAvgPrice(parseLong(fields[6]))  // 가중평균
                .openPrice(parseLong(fields[7]))         // 시가
                .highPrice(parseLong(fields[8]))         // 고가
                .lowPrice(parseLong(fields[9]))          // 저가
                .tradeVolume(parseLong(fields[12]))      // 체결거래량
                .accumulatedVolume(parseLong(fields[13])) // 누적거래량
                .accumulatedAmount(parseLong(fields[14])) // 누적거래대금
                .build();
    }
    
    private static long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
