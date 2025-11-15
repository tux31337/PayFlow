package com.truvis.stock.domain;

import com.truvis.common.model.AggregateRoot;
import com.truvis.common.model.vo.StockCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 종목 집합체 루트
 * - 종목의 기본 정보와 현재가를 관리
 * - 가격 업데이트 정책을 도메인 로직으로 캡슐화
 */
@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 종목 코드 (예: "005930" - 삼성전자)
     * Transaction 도메인과 공유하는 VO
     */
    @Embedded
    private StockCode stockCode;

    /**
     * 종목명 (예: "삼성전자")
     */
    @Embedded
    private StockName name;

    /**
     * 시장 구분 (KOSPI, KOSDAQ, KONEX)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Market market;

    /**
     * 업종 (예: "반도체", "자동차")
     */
    @Embedded
    private Sector sector;

    /**
     * 현재가
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "current_price"))
    private CurrentPrice currentPrice;

    /**
     * 가격 업데이트 시각
     * - 가격이 언제 마지막으로 업데이트되었는지 추적
     */
    @Column(nullable = false)
    private LocalDateTime priceUpdatedAt;

    /**
     * 생성 시각
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ==================== 생성자 ====================

    private Stock(
            StockCode stockCode,
            StockName name,
            Market market,
            Sector sector,
            CurrentPrice currentPrice
    ) {
        this.stockCode = Objects.requireNonNull(stockCode, "종목 코드는 필수입니다");
        this.name = Objects.requireNonNull(name, "종목명은 필수입니다");
        this.market = Objects.requireNonNull(market, "시장 구분은 필수입니다");
        this.sector = Objects.requireNonNull(sector, "업종은 필수입니다");
        this.currentPrice = Objects.requireNonNull(currentPrice, "현재가는 필수입니다");
        this.priceUpdatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 정적 팩토리 메서드 - 종목 생성
     */
    public static Stock create(
            StockCode stockCode,
            StockName name,
            Market market,
            Sector sector,
            CurrentPrice currentPrice
    ) {
        return new Stock(stockCode, name, market, sector, currentPrice);
    }

    // ==================== 도메인 로직 ====================

    /**
     * 가격 업데이트
     * - 비즈니스 규칙: 가격은 항상 양수여야 함 (VO에서 검증)
     * - 업데이트 시각을 자동으로 기록
     */
    public void updatePrice(CurrentPrice newPrice) {
        Objects.requireNonNull(newPrice, "새로운 가격은 필수입니다");
        this.currentPrice = newPrice;
        this.priceUpdatedAt = LocalDateTime.now();
    }

    /**
     * 가격이 오래되었는가?
     * - 비즈니스 규칙: 5분 이상 지난 가격은 stale로 간주
     * - WebSocket 연동 전까지는 배치로 업데이트
     */
    public boolean isPriceStale() {
        return priceUpdatedAt.isBefore(LocalDateTime.now().minusMinutes(5));
    }

    /**
     * 가격 변동률 계산
     * - 이전 가격 대비 현재 가격의 변동률 (%)
     */
    public double calculateChangeRate(CurrentPrice previousPrice) {
        return currentPrice.calculateChangeRate(previousPrice);
    }

    /**
     * 코스피 종목인가?
     */
    public boolean isKospi() {
        return market == Market.KOSPI;
    }

    /**
     * 코스닥 종목인가?
     */
    public boolean isKosdaq() {
        return market == Market.KOSDAQ;
    }
}