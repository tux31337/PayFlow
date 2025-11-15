package com.truvis.stock.repository;

import com.truvis.common.model.vo.StockCode;
import com.truvis.stock.domain.Market;
import com.truvis.stock.domain.Sector;
import com.truvis.stock.domain.Stock;

import java.util.List;
import java.util.Optional;

/**
 * 종목 저장소 인터페이스 (도메인 레이어)
 * - 집합체 루트인 Stock의 영속성 관리
 * - 도메인 의미 있는 메서드만 정의
 * - 인프라 기술(JPA)에 의존하지 않음
 */
public interface StockRepository {

    // ==================== 기본 CRUD ====================

    /**
     * 종목 저장 (생성/수정)
     * @param stock 저장할 종목
     * @return 저장된 종목
     */
    Stock save(Stock stock);

    /**
     * ID로 종목 조회
     * @param id 종목 ID
     * @return 종목 (없으면 Optional.empty())
     */
    Optional<Stock> findById(Long id);

    /**
     * 종목 코드로 종목 조회 (가장 중요한 메서드)
     * @param stockCode 종목 코드
     * @return 종목 (없으면 Optional.empty())
     */
    Optional<Stock> findByStockCode(StockCode stockCode);

    /**
     * 모든 종목 조회
     * @return 전체 종목 리스트
     */
    List<Stock> findAll();

    /**
     * 종목 삭제
     * @param stock 삭제할 종목
     */
    void delete(Stock stock);

    // ==================== 존재 여부 확인 ====================

    /**
     * 종목 코드 존재 여부 확인
     * @param stockCode 종목 코드
     * @return 존재하면 true
     */
    boolean existsByStockCode(StockCode stockCode);

    // ==================== 도메인 의미 있는 조회 ====================

    /**
     * 시장별 종목 조회
     * @param market 시장 (KOSPI, KOSDAQ, KONEX)
     * @return 해당 시장의 종목 리스트
     */
    List<Stock> findByMarket(Market market);

    /**
     * 섹터별 종목 조회
     * @param sector 섹터
     * @return 해당 섹터의 종목 리스트
     */
    List<Stock> findBySector(Sector sector);

    /**
     * 시장 + 섹터 조합 조회
     * @param market 시장
     * @param sector 섹터
     * @return 조건에 맞는 종목 리스트
     */
    List<Stock> findByMarketAndSector(Market market, Sector sector);

    /**
     * 종목명으로 검색 (자동완성용)
     * - 부분 일치 검색 (LIKE)
     * @param keyword 검색 키워드
     * @return 종목명에 키워드가 포함된 종목 리스트
     */
    List<Stock> searchByNameContaining(String keyword);

    /**
     * 여러 종목 코드로 일괄 조회 (포트폴리오, 관심종목용)
     * @param stockCodes 종목 코드 리스트
     * @return 해당 종목들
     */
    List<Stock> findAllByStockCodes(List<StockCode> stockCodes);

    // ==================== 카운트 ====================

    /**
     * 전체 종목 수
     * @return 종목 개수
     */
    long count();

    /**
     * 시장별 종목 수
     * @param market 시장
     * @return 해당 시장의 종목 개수
     */
    long countByMarket(Market market);
}