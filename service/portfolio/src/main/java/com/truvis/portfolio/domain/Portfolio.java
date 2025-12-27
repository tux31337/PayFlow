package com.truvis.portfolio.domain;

import com.truvis.common.model.AggregateRoot;
import com.truvis.common.model.vo.Money;
import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.common.model.vo.StockCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Portfolio Aggregate Root
 *
 * 사용자의 투자 포트폴리오 관리:
 * - 현금 잔고 관리
 * - 보유 종목 관리 (최대 50개)
 * - 포트폴리오 평가액/수익률 계산
 */
@Entity
@Table(name = "portfolios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio extends AggregateRoot<Long> {

    private static final int MAX_HOLDINGS = 50;
    private static final Money INITIAL_CASH = Money.of(10_000_000L); // 초기 1000만원

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소유자 사용자 ID
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * 포트폴리오 이름
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 현금 잔고
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cash_balance"))
    private Money cashBalance;

    /**
     * 보유 종목 목록
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "portfolio_id")
    private List<Holding> holdings = new ArrayList<>();

    // createdAt, updatedAt은 BaseEntity에서 자동 관리

    // ==================== 생성자 ====================

    private Portfolio(Long userId, String name, Money initialCash) {
        this.userId = Objects.requireNonNull(userId, "사용자 ID는 필수입니다");
        this.name = Objects.requireNonNull(name, "포트폴리오 이름은 필수입니다");
        this.cashBalance = Objects.requireNonNull(initialCash, "초기 자금은 필수입니다");
        this.holdings = new ArrayList<>();
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 새 포트폴리오 생성 (기본 자금 1000만원)
     */
    public static Portfolio create(Long userId, String name) {
        return new Portfolio(userId, name, INITIAL_CASH);
    }

    /**
     * 새 포트폴리오 생성 (초기 자금 지정)
     */
    public static Portfolio create(Long userId, String name, Money initialCash) {
        return new Portfolio(userId, name, initialCash);
    }

    // ==================== 현금 관리 ====================

    /**
     * 현금 입금
     */
    public void deposit(Money amount) {
        Objects.requireNonNull(amount, "입금액은 필수입니다");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("입금액은 0보다 커야 합니다");
        }

        this.cashBalance = this.cashBalance.add(amount);
        // updatedAt은 JPA Auditing이 자동 처리
    }

    /**
     * 현금 출금
     */
    public void withdraw(Money amount) {
        Objects.requireNonNull(amount, "출금액은 필수입니다");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("출금액은 0보다 커야 합니다");
        }
        if (this.cashBalance.isLessThan(amount)) {
            throw new IllegalArgumentException(
                    String.format(
                            "잔고가 부족합니다. 현재: %s원, 요청: %s원",
                            this.cashBalance.getValue(),
                            amount.getValue()
                    )
            );
        }

        this.cashBalance = this.cashBalance.subtract(amount);
    }

    /**
     * 잔고 확인
     */
    public boolean hasEnoughCash(Money requiredAmount) {
        return this.cashBalance.isGreaterThanOrEqual(requiredAmount);
    }

    // ==================== 종목 매수 ====================

    /**
     * 종목 매수
     *
     * @param stockCode 종목 코드
     * @param quantity 매수 수량
     * @param price 매수 가격
     */
    public void buyStock(StockCode stockCode, Quantity quantity, Price price) {
        Objects.requireNonNull(stockCode, "종목 코드는 필수입니다");
        Objects.requireNonNull(quantity, "수량은 필수입니다");
        Objects.requireNonNull(price, "가격은 필수입니다");

        // 1. 필요 금액 계산
        Money requiredAmount = price.multiply(quantity);

        // 2. 잔고 확인
        if (!hasEnoughCash(requiredAmount)) {
            throw new IllegalArgumentException(
                    String.format(
                            "잔고가 부족합니다. 현재: %s원, 필요: %s원",
                            this.cashBalance.getValue(),
                            requiredAmount.getValue()
                    )
            );
        }

        // 3. 기존 보유 종목 찾기
        Optional<Holding> existingHolding = findHolding(stockCode);

        if (existingHolding.isPresent()) {
            // 3-1. 이미 보유 중 → 추가 매수
            existingHolding.get().buy(quantity, price);
        } else {
            // 3-2. 신규 매수 → 새 Holding 생성
            if (holdings.size() >= MAX_HOLDINGS) {
                throw new IllegalStateException(
                        String.format("포트폴리오는 최대 %d개 종목까지만 보유할 수 있습니다", MAX_HOLDINGS)
                );
            }
            Holding newHolding = Holding.create(stockCode, quantity, price);
            holdings.add(newHolding);
        }

        // 4. 현금 차감
        this.cashBalance = this.cashBalance.subtract(requiredAmount);
    }

    // ==================== 종목 매도 ====================

    /**
     * 종목 매도
     *
     * @param stockCode 종목 코드
     * @param quantity 매도 수량
     * @param price 매도 가격
     */
    public void sellStock(StockCode stockCode, Quantity quantity, Price price) {
        Objects.requireNonNull(stockCode, "종목 코드는 필수입니다");
        Objects.requireNonNull(quantity, "수량은 필수입니다");
        Objects.requireNonNull(price, "가격은 필수입니다");

        // 1. 보유 종목 찾기
        Holding holding = findHolding(stockCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("보유하지 않은 종목입니다: %s", stockCode.getValue())
                ));

        // 2. 보유 수량 확인
        if (!holding.canSell(quantity)) {
            throw new IllegalArgumentException(
                    String.format(
                            "보유 수량이 부족합니다. 보유: %d주, 매도 요청: %d주",
                            holding.getQuantity().getValue(),
                            quantity.getValue()
                    )
            );
        }

        // 3. 매도 실행
        holding.sell(quantity);

        // 4. 현금 증가
        Money sellAmount = price.multiply(quantity);
        this.cashBalance = this.cashBalance.add(sellAmount);

        // 5. 전량 매도 시 holding 제거
        if (holding.isEmpty()) {
            holdings.remove(holding);
        }
    }

    /**
     * 특정 종목을 보유하고 있는가?
     */
    public boolean hasStock(StockCode stockCode) {
        return findHolding(stockCode).isPresent();
    }

    /**
     * 특정 종목의 보유 수량 조회
     */
    public Quantity getHoldingQuantity(StockCode stockCode) {
        return findHolding(stockCode)
                .map(Holding::getQuantity)
                .orElse(Quantity.of(0));
    }

    // ==================== 조회 메서드 ====================

    /**
     * 보유 종목 찾기
     */
    private Optional<Holding> findHolding(StockCode stockCode) {
        return holdings.stream()
                .filter(h -> h.getStockCode().equals(stockCode))
                .findFirst();
    }

    /**
     * 총 투자 금액 계산 (모든 종목의 총 매수 금액 합계)
     */
    public Money calculateTotalInvestedAmount() {
        return holdings.stream()
                .map(Holding::getTotalCost)
                .reduce(Money.ZERO, Money::add);
    }

    /**
     * 보유 종목 개수
     */
    public int getHoldingCount() {
        return holdings.size();
    }

    /**
     * 포트폴리오가 비어있는가?
     */
    public boolean isEmpty() {
        return holdings.isEmpty();
    }

    /**
     * 포트폴리오 요약 정보
     */
    public String getSummary() {
        return String.format(
                "Portfolio[%s] - 현금: %s원, 보유 종목: %d개, 총 투자액: %s원",
                name,
                cashBalance.getValue(),
                holdings.size(),
                calculateTotalInvestedAmount().getValue()
        );
    }
}
