package com.truvis.stock.repository;

import com.truvis.stock.domain.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주식 가격 히스토리 레포지토리
 */
@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {

    /**
     * 특정 종목의 시간 범위 조회
     */
    List<StockPriceHistory> findByStockCodeAndTradeTimeBetweenOrderByTradeTimeAsc(
            String stockCode,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * 특정 종목의 최근 N개 조회
     */
    List<StockPriceHistory> findTop100ByStockCodeOrderByTradeTimeDesc(String stockCode);
}
