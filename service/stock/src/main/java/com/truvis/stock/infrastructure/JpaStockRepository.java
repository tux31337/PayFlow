package com.truvis.stock.infrastructure;

import com.truvis.common.model.vo.StockCode;
import com.truvis.stock.domain.Market;
import com.truvis.stock.domain.Sector;
import com.truvis.stock.domain.Stock;
import com.truvis.stock.repository.StockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Stock Repository JPA 구현체
 */
public interface JpaStockRepository extends JpaRepository<Stock, Long>, StockRepository {

    // ==================== 기본 조회 ====================

    @Override
    Optional<Stock> findByStockCode(StockCode stockCode);

    @Override
    boolean existsByStockCode(StockCode stockCode);

    // ==================== 페이징 조회 ====================

    /**
     * 시장별 조회 (페이징)
     */
    @Override
    Page<Stock> findByMarket(Market market, Pageable pageable);

    /**
     * 시장별 조회 (전체)
     */
    @Override
    List<Stock> findByMarket(Market market);

    /**
     * 섹터별 조회
     */
    @Override
    List<Stock> findBySector(Sector sector);

    /**
     * 시장 + 섹터 조합 조회
     */
    @Override
    List<Stock> findByMarketAndSector(Market market, Sector sector);

    /**
     * 종목명 검색 (페이징)
     */
    @Query("SELECT s FROM Stock s WHERE s.name.value LIKE %:keyword%")
    @Override
    Page<Stock> searchByNameContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 여러 종목 코드로 일괄 조회
     */
    @Query("SELECT s FROM Stock s WHERE s.stockCode IN :stockCodes")
    @Override
    List<Stock> findAllByStockCodes(@Param("stockCodes") List<StockCode> stockCodes);

    /**
     * 시장별 종목 수
     */
    @Override
    long countByMarket(Market market);
}
