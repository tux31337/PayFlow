package com.truvis.portfolio.application;

import com.truvis.common.exception.PortfolioException;
import com.truvis.common.model.vo.Money;
import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.common.model.vo.StockCode;
import com.truvis.portfolio.domain.Holding;
import com.truvis.portfolio.domain.Portfolio;
import com.truvis.portfolio.model.HoldingResponse;
import com.truvis.portfolio.model.PortfolioResponse;
import com.truvis.portfolio.model.PortfolioSummary;
import com.truvis.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 포트폴리오 Application Service
 * 
 * 책임:
 * 1. 포트폴리오 생성/조회/삭제
 * 2. 잔고 관리 (입금/출금)
 * 3. 포트폴리오 분석 (총 평가액, 수익률 등)
 * 4. 매수/매도 가능 여부 검증
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioApplicationService {

    private final PortfolioRepository portfolioRepository;

    // ==================== 포트폴리오 생성 ====================

    /**
     * 새 포트폴리오 생성 (기본 자금 1000만원)
     */
    @Transactional
    public Portfolio createPortfolio(Long userId, String name) {
        log.info("포트폴리오 생성 요청 - userId: {}, name: {}", userId, name);

        // 이미 포트폴리오가 있는지 확인 (1인 1포트폴리오 정책)
        if (portfolioRepository.findByUserId(userId).isPresent()) {
            throw PortfolioException.alreadyExists(userId);
        }

        Portfolio portfolio = Portfolio.create(userId, name);
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        log.info("포트폴리오 생성 완료 - portfolioId: {}, userId: {}", 
                savedPortfolio.getId(), userId);

        return savedPortfolio;
    }

    /**
     * 새 포트폴리오 생성 (초기 자금 지정)
     */
    @Transactional
    public Portfolio createPortfolio(Long userId, String name, Money initialCash) {
        log.info("포트폴리오 생성 요청 - userId: {}, name: {}, initialCash: {}",
                userId, name, initialCash.getValue());

        if (portfolioRepository.findByUserId(userId).isPresent()) {
            throw PortfolioException.alreadyExists(userId);
        }

        Portfolio portfolio = Portfolio.create(userId, name, initialCash);
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        log.info("포트폴리오 생성 완료 - portfolioId: {}", savedPortfolio.getId());

        return savedPortfolio;
    }

    // ==================== 포트폴리오 조회 ====================

    /**
     * ID로 포트폴리오 조회
     */
    public Portfolio getPortfolio(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> PortfolioException.notFound(portfolioId));
    }

    /**
     * 사용자 ID로 포트폴리오 조회
     */
    public Portfolio getPortfolioByUserId(Long userId) {
        return portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> PortfolioException.notFound(userId));
    }

    /**
     * 사용자 포트폴리오 존재 여부 확인
     */
    public boolean hasPortfolio(Long userId) {
        return portfolioRepository.findByUserId(userId).isPresent();
    }

    /**
     * 포트폴리오 상세 응답 조회 (현재가 포함)
     * 
     * @param userId 사용자 ID
     * @param currentPrices 종목별 현재가 맵 (stockCode -> Price)
     */
    public PortfolioResponse getPortfolioResponse(Long userId, Map<String, Price> currentPrices) {
        Portfolio portfolio = getPortfolioByUserId(userId);
        return buildPortfolioResponse(portfolio, currentPrices);
    }

    // ==================== 잔고 관리 ====================

    /**
     * 현금 입금
     */
    @Transactional
    public void deposit(Long userId, Money amount) {
        log.info("입금 요청 - userId: {}, amount: {}", userId, amount.getValue());

        Portfolio portfolio = getPortfolioByUserId(userId);
        portfolio.deposit(amount);
        portfolioRepository.save(portfolio);

        log.info("입금 완료 - userId: {}, 현재 잔고: {}", userId, portfolio.getCashBalance().getValue());
    }

    /**
     * 현금 출금
     */
    @Transactional
    public void withdraw(Long userId, Money amount) {
        log.info("출금 요청 - userId: {}, amount: {}", userId, amount.getValue());

        Portfolio portfolio = getPortfolioByUserId(userId);
        portfolio.withdraw(amount);
        portfolioRepository.save(portfolio);

        log.info("출금 완료 - userId: {}, 현재 잔고: {}", userId, portfolio.getCashBalance().getValue());
    }

    /**
     * 현금 잔고 조회
     */
    public Money getCashBalance(Long userId) {
        Portfolio portfolio = getPortfolioByUserId(userId);
        return portfolio.getCashBalance();
    }

    // ==================== 검증 메서드 (Order 연동) ====================

    /**
     * 매수 가능 여부 확인
     * 
     * @param userId 사용자 ID
     * @param requiredAmount 필요 금액
     * @return 매수 가능 여부
     */
    public boolean canBuy(Long userId, Money requiredAmount) {
        try {
            Portfolio portfolio = getPortfolioByUserId(userId);
            return portfolio.hasEnoughCash(requiredAmount);
        } catch (PortfolioException e) {
            // 포트폴리오가 없으면 매수 불가
            return false;
        }
    }

    /**
     * 매수 가능 여부 검증 (예외 발생)
     */
    public void validateBuyOrder(Long userId, Money requiredAmount) {
        Portfolio portfolio = getPortfolioByUserId(userId);

        if (!portfolio.hasEnoughCash(requiredAmount)) {
            throw PortfolioException.insufficientBalance(
                    portfolio.getCashBalance().getValue().longValue(),
                    requiredAmount.getValue().longValue()
            );
        }
    }

    /**
     * 매도 가능 여부 확인
     * 
     * @param userId 사용자 ID
     * @param stockCode 종목 코드
     * @param quantity 매도 수량
     * @return 매도 가능 여부
     */
    public boolean canSell(Long userId, StockCode stockCode, Quantity quantity) {
        try {
            Portfolio portfolio = getPortfolioByUserId(userId);
            Quantity holdingQty = portfolio.getHoldingQuantity(stockCode);
            return holdingQty.isGreaterThanOrEqual(quantity);
        } catch (PortfolioException e) {
            return false;
        }
    }

    /**
     * 매도 가능 여부 검증 (예외 발생)
     */
    public void validateSellOrder(Long userId, StockCode stockCode, Quantity quantity) {
        Portfolio portfolio = getPortfolioByUserId(userId);

        if (!portfolio.hasStock(stockCode)) {
            throw PortfolioException.stockNotHeld(stockCode.getValue());
        }

        Quantity holdingQty = portfolio.getHoldingQuantity(stockCode);
        if (!holdingQty.isGreaterThanOrEqual(quantity)) {
            throw PortfolioException.insufficientHolding(
                    stockCode.getValue(),
                    holdingQty.getValue(),
                    quantity.getValue()
            );
        }
    }

    /**
     * 특정 종목 보유 수량 조회
     */
    public Quantity getHoldingQuantity(Long userId, StockCode stockCode) {
        Portfolio portfolio = getPortfolioByUserId(userId);
        return portfolio.getHoldingQuantity(stockCode);
    }

    // ==================== 포트폴리오 분석 ====================

    /**
     * 포트폴리오 요약 정보 조회
     * 
     * @param userId 사용자 ID
     * @param currentPrices 종목별 현재가 맵
     */
    public PortfolioSummary getPortfolioSummary(Long userId, Map<String, Price> currentPrices) {
        Portfolio portfolio = getPortfolioByUserId(userId);

        // 현재 평가액 계산
        Money totalEvaluation = calculateTotalEvaluation(portfolio, currentPrices);
        
        // 총 투자 금액
        Money totalInvested = portfolio.calculateTotalInvestedAmount();
        
        // 총 손익
        Money totalProfit = totalEvaluation.isGreaterThan(totalInvested) 
                ? totalEvaluation.subtract(totalInvested)
                : Money.ZERO;
        
        // 총 손익이 마이너스인 경우
        boolean isLoss = totalInvested.isGreaterThan(totalEvaluation);
        Money totalLoss = isLoss ? totalInvested.subtract(totalEvaluation) : Money.ZERO;
        
        // 수익률 계산
        double returnRate = calculateReturnRate(totalInvested, totalEvaluation);

        // 총 자산 = 현금 + 평가액
        Money totalAssets = portfolio.getCashBalance().add(totalEvaluation);

        return PortfolioSummary.builder()
                .portfolioId(portfolio.getId())
                .name(portfolio.getName())
                .cashBalance(portfolio.getCashBalance())
                .totalEvaluation(totalEvaluation)
                .totalInvested(totalInvested)
                .totalProfit(isLoss ? Money.ZERO : totalProfit)
                .totalLoss(totalLoss)
                .returnRate(returnRate)
                .totalAssets(totalAssets)
                .holdingCount(portfolio.getHoldingCount())
                .build();
    }

    /**
     * 총 평가액 계산
     */
    private Money calculateTotalEvaluation(Portfolio portfolio, Map<String, Price> currentPrices) {
        return portfolio.getHoldings().stream()
                .map(holding -> {
                    Price currentPrice = currentPrices.get(holding.getStockCode().getValue());
                    if (currentPrice != null) {
                        return holding.calculateCurrentValue(currentPrice);
                    }
                    // 현재가 정보 없으면 투자 금액으로 계산
                    return holding.getTotalCost();
                })
                .reduce(Money.ZERO, Money::add);
    }

    /**
     * 수익률 계산
     */
    private double calculateReturnRate(Money totalInvested, Money totalEvaluation) {
        if (totalInvested.isZero()) {
            return 0.0;
        }

        double invested = totalInvested.getValue().doubleValue();
        double evaluation = totalEvaluation.getValue().doubleValue();

        return ((evaluation - invested) / invested) * 100.0;
    }

    // ==================== Response 빌더 ====================

    /**
     * PortfolioResponse 생성
     */
    private PortfolioResponse buildPortfolioResponse(Portfolio portfolio, Map<String, Price> currentPrices) {
        // 보유 종목 응답 목록
        List<HoldingResponse> holdingResponses = portfolio.getHoldings().stream()
                .map(holding -> buildHoldingResponse(holding, currentPrices))
                .collect(Collectors.toList());

        // 총 평가액
        Money totalEvaluation = calculateTotalEvaluation(portfolio, currentPrices);

        // 총 투자액
        Money totalInvested = portfolio.calculateTotalInvestedAmount();

        // 수익률
        double returnRate = calculateReturnRate(totalInvested, totalEvaluation);

        // 총 자산
        Money totalAssets = portfolio.getCashBalance().add(totalEvaluation);

        return PortfolioResponse.builder()
                .portfolioId(portfolio.getId())
                .userId(portfolio.getUserId())
                .name(portfolio.getName())
                .cashBalance(portfolio.getCashBalance().getValue())
                .totalEvaluation(totalEvaluation.getValue())
                .totalInvested(totalInvested.getValue())
                .returnRate(returnRate)
                .totalAssets(totalAssets.getValue())
                .holdingCount(portfolio.getHoldingCount())
                .holdings(holdingResponses)
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    /**
     * HoldingResponse 생성
     */
    private HoldingResponse buildHoldingResponse(Holding holding, Map<String, Price> currentPrices) {
        Price currentPrice = currentPrices.get(holding.getStockCode().getValue());
        
        Money currentValue;
        Money profit;
        double returnRate;

        if (currentPrice != null) {
            currentValue = holding.calculateCurrentValue(currentPrice);
            profit = holding.calculateProfit(currentPrice);
            returnRate = holding.calculateReturnRate(currentPrice);
        } else {
            // 현재가 없으면 평균 매수가 기준
            currentValue = holding.getTotalCost();
            profit = Money.ZERO;
            returnRate = 0.0;
        }

        return HoldingResponse.builder()
                .stockCode(holding.getStockCode().getValue())
                .quantity(holding.getQuantity().getValue())
                .averagePrice(holding.getAveragePrice().getValue())
                .currentPrice(currentPrice != null ? currentPrice.getValue() : holding.getAveragePrice().getValue())
                .totalCost(holding.getTotalCost().getValue())
                .currentValue(currentValue.getValue())
                .profit(profit.getValue())
                .returnRate(returnRate)
                .firstPurchasedAt(holding.getFirstPurchasedAt())
                .build();
    }

    // ==================== 삭제 ====================

    /**
     * 포트폴리오 삭제
     */
    @Transactional
    public void deletePortfolio(Long userId) {
        log.info("포트폴리오 삭제 요청 - userId: {}", userId);

        Portfolio portfolio = getPortfolioByUserId(userId);
        
        if (!portfolio.isEmpty()) {
            throw PortfolioException.cannotDeleteWithHoldings();
        }

        portfolioRepository.delete(portfolio);
        log.info("포트폴리오 삭제 완료 - userId: {}", userId);
    }
}
