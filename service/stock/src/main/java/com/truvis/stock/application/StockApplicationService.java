package com.truvis.stock.application;

import com.truvis.common.model.vo.StockCode;
import com.truvis.stock.domain.*;
import com.truvis.stock.model.StockDetailResponse;
import com.truvis.stock.model.StockResponse;
import com.truvis.stock.model.StockSearchResponse;
import com.truvis.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 종목 Application Service
 * - 종목 관련 유즈케이스 처리
 * - 트랜잭션 경계 관리
 * - 도메인 객체와 DTO 변환
 */
@Service
@Transactional(readOnly = true)  // 기본은 읽기 전용
@RequiredArgsConstructor
@Slf4j
public class StockApplicationService {

    private final StockRepository stockRepository;
    private final StockPriceProvider stockPriceProvider;  // 가격 조회 Provider

    // ==================== 조회 ====================

    /**
     * 종목 검색 (자동완성용)
     * - 종목명으로 부분 검색
     *
     * @param keyword 검색 키워드
     * @return 검색된 종목 리스트
     */
    public List<StockSearchResponse> searchStocks(String keyword) {
        log.info("종목 검색: keyword={}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        List<Stock> stocks = stockRepository.searchByNameContaining(keyword.trim());

        return stocks.stream()
                .map(this::toSearchResponse)
                .collect(Collectors.toList());
    }

    /**
     * 종목 상세 조회 (실시간 가격 포함)
     * - Repository에서 기본 정보 조회
     * - Provider로 실시간 가격 조회
     *
     * @param stockCodeValue 종목 코드 (예: "005930")
     * @return 종목 상세 정보
     */
    public StockDetailResponse getStockDetail(String stockCodeValue) {
        log.info("종목 상세 조회: stockCode={}", stockCodeValue);

        StockCode stockCode = StockCode.of(stockCodeValue);
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "종목을 찾을 수 없습니다: " + stockCodeValue));

        // 실시간 가격 조회
        CurrentPrice realtimePrice = stockPriceProvider.getCurrentPrice(stockCode);

        // 가격이 오래되었으면 업데이트
        if (stock.isPriceStale()) {
            updateStockPrice(stock, realtimePrice);
        }

        return toDetailResponse(stock, realtimePrice);
    }

    /**
     * 시장별 종목 조회
     *
     * @param market 시장 (KOSPI, KOSDAQ, KONEX)
     * @return 해당 시장의 종목 리스트
     */
    public List<StockResponse> getStocksByMarket(Market market) {
        log.info("시장별 종목 조회: market={}", market);

        List<Stock> stocks = stockRepository.findByMarket(market);

        return stocks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 섹터별 종목 조회
     *
     * @param sectorValue 섹터 (예: "반도체")
     * @return 해당 섹터의 종목 리스트
     */
    public List<StockResponse> getStocksBySector(String sectorValue) {
        log.info("섹터별 종목 조회: sector={}", sectorValue);

        Sector sector = Sector.of(sectorValue);
        List<Stock> stocks = stockRepository.findBySector(sector);

        return stocks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 여러 종목 조회 (포트폴리오, 관심종목용)
     *
     * @param stockCodeValues 종목 코드 리스트
     * @return 종목 리스트
     */
    public List<StockResponse> getStocks(List<String> stockCodeValues) {
        log.info("여러 종목 조회: count={}", stockCodeValues.size());

        List<StockCode> stockCodes = stockCodeValues.stream()
                .map(StockCode::of)
                .collect(Collectors.toList());

        List<Stock> stocks = stockRepository.findAllByStockCodes(stockCodes);

        return stocks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 전체 종목 조회
     *
     * @return 전체 종목 리스트
     */
    public List<StockResponse> getAllStocks() {
        log.info("전체 종목 조회");

        List<Stock> stocks = stockRepository.findAll();

        return stocks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 종목 존재 여부 확인
     *
     * @param stockCodeValue 종목 코드
     * @return 존재하면 true
     */
    public boolean existsStock(String stockCodeValue) {
        log.info("종목 존재 확인: stockCode={}", stockCodeValue);

        StockCode stockCode = StockCode.of(stockCodeValue);
        return stockRepository.existsByStockCode(stockCode);
    }

    /**
     * 시장별 종목 수
     *
     * @param market 시장
     * @return 종목 개수
     */
    public long countByMarket(Market market) {
        log.info("시장별 종목 수 조회: market={}", market);
        return stockRepository.countByMarket(market);
    }

    // ==================== 등록/수정 ====================

    /**
     * 종목 등록 (관리자 기능)
     *
     * @param stockCodeValue 종목 코드
     * @param nameValue 종목명
     * @param market 시장
     * @param sectorValue 섹터
     * @return 등록된 종목 정보
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 필요
    public StockResponse registerStock(
            String stockCodeValue,
            String nameValue,
            Market market,
            String sectorValue
    ) {
        log.info("종목 등록: stockCode={}, name={}", stockCodeValue, nameValue);

        StockCode stockCode = StockCode.of(stockCodeValue);

        // 중복 체크
        if (stockRepository.existsByStockCode(stockCode)) {
            throw new IllegalArgumentException("이미 등록된 종목입니다: " + stockCodeValue);
        }

        // 현재가 조회
        CurrentPrice currentPrice = stockPriceProvider.getCurrentPrice(stockCode);

        // 도메인 객체 생성
        Stock stock = Stock.create(
                stockCode,
                StockName.of(nameValue),
                market,
                Sector.of(sectorValue),
                currentPrice
        );

        // 저장
        Stock savedStock = stockRepository.save(stock);

        log.info("종목 등록 완료: id={}, stockCode={}", savedStock.getId(), stockCodeValue);

        return toResponse(savedStock);
    }

    /**
     * 종목 가격 업데이트 (배치용)
     *
     * @param stockCodeValue 종목 코드
     */
    @Transactional
    public void updateStockPrice(String stockCodeValue) {
        log.info("종목 가격 업데이트: stockCode={}", stockCodeValue);

        StockCode stockCode = StockCode.of(stockCodeValue);
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "종목을 찾을 수 없습니다: " + stockCodeValue));

        // 실시간 가격 조회
        CurrentPrice newPrice = stockPriceProvider.getCurrentPrice(stockCode);

        // 도메인 메서드로 가격 업데이트
        stock.updatePrice(newPrice);

        // 변경 감지(Dirty Checking)로 자동 저장
        log.info("가격 업데이트 완료: stockCode={}, price={}",
                stockCodeValue, newPrice.getValue());
    }

    /**
     * 모든 종목 가격 일괄 업데이트 (배치용)
     */
    @Transactional
    public void updateAllStockPrices() {
        log.info("전체 종목 가격 업데이트 시작");

        List<Stock> allStocks = stockRepository.findAll();

        int updatedCount = 0;
        int failedCount = 0;

        for (Stock stock : allStocks) {
            try {
                CurrentPrice newPrice = stockPriceProvider.getCurrentPrice(stock.getStockCode());
                stock.updatePrice(newPrice);
                updatedCount++;
            } catch (Exception e) {
                failedCount++;
                log.error("가격 업데이트 실패: stockCode={}, error={}",
                        stock.getStockCode().getValue(), e.getMessage());
            }
        }

        log.info("전체 종목 가격 업데이트 완료: total={}, updated={}, failed={}",
                allStocks.size(), updatedCount, failedCount);
    }

    /**
     * 종목 삭제 (관리자 기능)
     *
     * @param stockCodeValue 종목 코드
     */
    @Transactional
    public void deleteStock(String stockCodeValue) {
        log.info("종목 삭제: stockCode={}", stockCodeValue);

        StockCode stockCode = StockCode.of(stockCodeValue);
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "종목을 찾을 수 없습니다: " + stockCodeValue));

        stockRepository.delete(stock);

        log.info("종목 삭제 완료: stockCode={}", stockCodeValue);
    }

    // ==================== Private 헬퍼 메서드 ====================

    /**
     * 가격 업데이트 (내부용)
     * - 트랜잭션 내에서 호출되므로 @Transactional 불필요
     */
    private void updateStockPrice(Stock stock, CurrentPrice newPrice) {
        stock.updatePrice(newPrice);
        // 변경 감지로 자동 저장
    }

    // ==================== DTO 변환 메서드 ====================

    /**
     * Stock → StockSearchResponse 변환
     */
    private StockSearchResponse toSearchResponse(Stock stock) {
        return new StockSearchResponse(
                stock.getStockCode().getValue(),
                stock.getName().getValue(),
                stock.getMarket().name()
        );
    }

    /**
     * Stock → StockResponse 변환
     */
    private StockResponse toResponse(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getStockCode().getValue(),
                stock.getName().getValue(),
                stock.getMarket().name(),
                stock.getSector().getValue(),
                stock.getCurrentPrice().getValue().toString(),  // BigDecimal → String
                stock.getPriceUpdatedAt()
        );
    }

    /**
     * Stock → StockDetailResponse 변환 (실시간 가격 포함)
     */
    private StockDetailResponse toDetailResponse(Stock stock, CurrentPrice realtimePrice) {
        // 변동 계산
        BigDecimal changeAmount = realtimePrice.calculateChangeAmount(stock.getCurrentPrice());
        double changeRate = realtimePrice.calculateChangeRate(stock.getCurrentPrice());

        return new StockDetailResponse(
                stock.getId(),
                stock.getStockCode().getValue(),
                stock.getName().getValue(),
                stock.getMarket().name(),
                stock.getSector().getValue(),

                // 가격 정보 - 포맷팅된 문자열
                realtimePrice.formatKorean(),              // "71,000"
                stock.getCurrentPrice().formatKorean(),    // "70,500"
                formatChangeAmount(changeAmount),          // "+500"
                formatChangeRate(changeRate),              // "+0.71%"

                stock.getPriceUpdatedAt(),
                stock.isKospi(),
                stock.isKosdaq()
        );
    }

    // ==================== 포맷팅 헬퍼 메서드 ====================

    /**
     * 변동금액 포맷팅
     * - 양수: "+500"
     * - 음수: "-500"
     * - 0: "0"
     */
    private String formatChangeAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + amount.toString();
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return amount.toString();  // 이미 - 부호 포함
        } else {
            return "0";
        }
    }

    /**
     * 변동률 포맷팅
     * - 양수: "+5.50%"
     * - 음수: "-3.20%"
     * - 0: "0.00%"
     */
    private String formatChangeRate(double rate) {
        if (rate > 0) {
            return String.format("+%.2f%%", rate);
        } else if (rate < 0) {
            return String.format("%.2f%%", rate);
        } else {
            return "0.00%";
        }
    }
}