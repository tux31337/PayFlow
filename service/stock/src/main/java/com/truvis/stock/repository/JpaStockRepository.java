package com.truvis.stock.infrastructure;

import com.truvis.common.model.vo.StockCode;
import com.truvis.stock.domain.Market;
import com.truvis.stock.domain.Sector;
import com.truvis.stock.domain.Stock;
import com.truvis.stock.repository.StockRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Stock Repository JPA 구현체
 * - Spring Data JPA가 자동으로 구현체 생성
 * - 도메인 Repository 인터페이스를 확장
 */
public interface JpaStockRepository extends JpaRepository<Stock, Long>, StockRepository {

    // ==================== 기본 CRUD ====================
    // JpaRepository가 제공: save(), findById(), findAll(), delete(), count()

    // ==================== 커스텀 메서드 ====================

    /**
     * 종목 코드로 조회
     * - 메서드 이름으로 쿼리 자동 생성 (Query Method)
     */
    @Override
    Optional<Stock> findByStockCode(StockCode stockCode);

    /**
     * 종목 코드 존재 여부
     */
    @Override
    boolean existsByStockCode(StockCode stockCode);

    /**
     * 시장별 조회
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
     * 종목명 검색 (LIKE 검색)
     * - name.value에 접근 (Embedded 객체)
     */
    @Query("SELECT s FROM Stock s WHERE s.name.value LIKE %:keyword%")
    @Override
    List<Stock> searchByNameContaining(@Param("keyword") String keyword);

    /**
     * 여러 종목 코드로 일괄 조회
     * - IN 절 사용
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