package com.truvis.stock.repository;

import com.truvis.common.model.vo.StockCode;
import com.truvis.stock.domain.Market;
import com.truvis.stock.domain.Sector;
import com.truvis.stock.domain.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 종목 저장소 인터페이스 (도메인 레이어)
 * - save(), findById(), findAll(), delete()는 JpaRepository에서 제공
 */
public interface StockRepository {

    /**
     * 종목 코드로 종목 조회
     */
    Optional<Stock> findByStockCode(StockCode stockCode);

    // ==================== 존재 여부 확인 ====================

    /**
     * 종목 코드 존재 여부 확인
     */
    boolean existsByStockCode(StockCode stockCode);

    // ==================== 페이징 조회 ====================

    /**
     * 시장별 종목 조회 (페이징)
     */
    Page<Stock> findByMarket(Market market, Pageable pageable);

    /**
     * 시장별 종목 조회 (전체) - 배치용
     */
    List<Stock> findByMarket(Market market);

    /**
     * 섹터별 종목 조회
     */
    List<Stock> findBySector(Sector sector);

    /**
     * 시장 + 섹터 조합 조회
     */
    List<Stock> findByMarketAndSector(Market market, Sector sector);

    /**
     * 종목명으로 검색 (페이징)
     */
    Page<Stock> searchByNameContaining(String keyword, Pageable pageable);

    /**
     * 여러 종목 코드로 일괄 조회
     */
    List<Stock> findAllByStockCodes(List<StockCode> stockCodes);

    // ==================== 카운트 ====================

    /**
     * 시장별 종목 수
     */
    long countByMarket(Market market);
}
