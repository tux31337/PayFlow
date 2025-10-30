package com.truvis.stock.domain;

import com.truvis.common.model.vo.StockCode;

import java.util.List;
import java.util.Map;

public interface StockPriceProvider {
    /**
     * 단일 종목 현재가 조회
     *
     * @param stockCode 종목 코드
     * @return 현재가
     * @throws IllegalArgumentException 종목 코드가 null인 경우
     * @throws PriceProviderException API 호출 실패, 네트워크 오류 등
     */
    CurrentPrice getCurrentPrice(StockCode stockCode);

    /**
     * 여러 종목 현재가 일괄 조회
     * - 성능 최적화: 1건씩 호출보다 빠름
     * - API 호출 횟수 절약
     *
     * @param stockCodes 종목 코드 목록
     * @return 종목 코드별 현재가 맵
     * @throws IllegalArgumentException 종목 코드 목록이 null이거나 비어있는 경우
     */
    Map<StockCode, CurrentPrice> getCurrentPrices(List<StockCode> stockCodes);

    /**
     * Provider 타입 반환
     * - 로깅, 모니터링에 사용
     *
     * @return "MOCK", "KIS_API", "CACHED" 등
     */
    String getProviderType();

    /**
     * Provider가 정상 작동하는지 확인
     * - Health Check용
     *
     * @return 정상이면 true
     */
    default boolean isHealthy() {
        return true;
    }
}
