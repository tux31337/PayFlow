package com.truvis.controller.portfolio;

import com.truvis.common.model.vo.Money;
import com.truvis.common.model.vo.Price;
import com.truvis.common.security.SecurityUtils;
import com.truvis.portfolio.application.PortfolioApplicationService;
import com.truvis.portfolio.domain.Portfolio;
import com.truvis.portfolio.model.HoldingResponse;
import com.truvis.portfolio.model.PortfolioResponse;
import com.truvis.portfolio.model.PortfolioSummary;
import com.truvis.stock.application.StockApplicationService;
import com.truvis.stock.model.StockDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 포트폴리오 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioApplicationService portfolioService;
    private final StockApplicationService stockService;

    // ==================== 포트폴리오 조회 ====================

    /**
     * 내 포트폴리오 조회
     * GET /api/portfolios/me
     */
    @GetMapping("/me")
    public ResponseEntity<PortfolioResponse> getMyPortfolio() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("[Portfolio] 포트폴리오 조회 - userId: {}", userId);

        // 현재가 조회를 위한 맵 생성
        Map<String, Price> currentPrices = getCurrentPrices(userId);
        
        PortfolioResponse response = portfolioService.getPortfolioResponse(userId, currentPrices);
        return ResponseEntity.ok(response);
    }

    /**
     * 포트폴리오 요약 정보 조회
     * GET /api/portfolios/me/summary
     */
    @GetMapping("/me/summary")
    public ResponseEntity<PortfolioSummaryResponse> getPortfolioSummary() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("[Portfolio] 포트폴리오 요약 조회 - userId: {}", userId);

        Map<String, Price> currentPrices = getCurrentPrices(userId);
        PortfolioSummary summary = portfolioService.getPortfolioSummary(userId, currentPrices);

        PortfolioSummaryResponse response = PortfolioSummaryResponse.from(summary);
        return ResponseEntity.ok(response);
    }

    /**
     * 보유 종목 목록 조회
     * GET /api/portfolios/me/holdings
     */
    @GetMapping("/me/holdings")
    public ResponseEntity<List<HoldingResponse>> getHoldings() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("[Portfolio] 보유 종목 조회 - userId: {}", userId);

        Map<String, Price> currentPrices = getCurrentPrices(userId);
        PortfolioResponse response = portfolioService.getPortfolioResponse(userId, currentPrices);

        return ResponseEntity.ok(response.getHoldings());
    }

    /**
     * 현금 잔고 조회
     * GET /api/portfolios/me/cash
     */
    @GetMapping("/me/cash")
    public ResponseEntity<CashBalanceResponse> getCashBalance() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("[Portfolio] 현금 잔고 조회 - userId: {}", userId);

        Money cashBalance = portfolioService.getCashBalance(userId);
        return ResponseEntity.ok(new CashBalanceResponse(cashBalance.getValue()));
    }

    // ==================== 포트폴리오 생성 ====================

    /**
     * 포트폴리오 생성
     * POST /api/portfolios
     */
    @PostMapping
    public ResponseEntity<PortfolioCreateResponse> createPortfolio(
            @RequestBody CreatePortfolioRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("[Portfolio] 포트폴리오 생성 - userId: {}, name: {}", userId, request.getName());

        Portfolio portfolio;
        if (request.getInitialCash() != null && request.getInitialCash() > 0) {
            portfolio = portfolioService.createPortfolio(
                    userId,
                    request.getName(),
                    Money.of(request.getInitialCash())
            );
        } else {
            portfolio = portfolioService.createPortfolio(userId, request.getName());
        }

        PortfolioCreateResponse response = new PortfolioCreateResponse(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getCashBalance().getValue()
        );

        return ResponseEntity.ok(response);
    }

    // ==================== 입출금 ====================

    /**
     * 현금 입금
     * POST /api/portfolios/me/deposit
     */
    @PostMapping("/me/deposit")
    public ResponseEntity<CashBalanceResponse> deposit(
            @RequestBody DepositRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("[Portfolio] 입금 - userId: {}, amount: {}", userId, request.getAmount());

        portfolioService.deposit(userId, Money.of(request.getAmount()));
        Money newBalance = portfolioService.getCashBalance(userId);

        return ResponseEntity.ok(new CashBalanceResponse(newBalance.getValue()));
    }

    /**
     * 현금 출금
     * POST /api/portfolios/me/withdraw
     */
    @PostMapping("/me/withdraw")
    public ResponseEntity<CashBalanceResponse> withdraw(
            @RequestBody WithdrawRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("[Portfolio] 출금 - userId: {}, amount: {}", userId, request.getAmount());

        portfolioService.withdraw(userId, Money.of(request.getAmount()));
        Money newBalance = portfolioService.getCashBalance(userId);

        return ResponseEntity.ok(new CashBalanceResponse(newBalance.getValue()));
    }

    // ==================== Helper Methods ====================

    /**
     * 보유 종목의 현재가 조회
     */
    private Map<String, Price> getCurrentPrices(Long userId) {
        Map<String, Price> prices = new HashMap<>();

        try {
            Portfolio portfolio = portfolioService.getPortfolioByUserId(userId);

            for (var holding : portfolio.getHoldings()) {
                String stockCode = holding.getStockCode().getValue();
                try {
                    StockDetailResponse stock = stockService.getStockDetail(stockCode);
                    // "71,000" → 71000
                    String priceStr = stock.getCurrentPrice().replace(",", "");
                    prices.put(stockCode, Price.of(Long.parseLong(priceStr)));
                } catch (Exception e) {
                    log.warn("[Portfolio] 종목 현재가 조회 실패: {}", stockCode, e);
                    // 현재가 조회 실패 시 평균 매수가 사용
                    prices.put(stockCode, holding.getAveragePrice());
                }
            }
        } catch (Exception e) {
            log.warn("[Portfolio] 포트폴리오 조회 실패: userId={}", userId, e);
        }

        return prices;
    }

    // ==================== Request/Response DTOs ====================

    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreatePortfolioRequest {
        private String name;
        private Long initialCash;  // 선택적
    }

    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DepositRequest {
        private long amount;
    }

    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WithdrawRequest {
        private long amount;
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class CashBalanceResponse {
        private java.math.BigDecimal balance;
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class PortfolioCreateResponse {
        private Long portfolioId;
        private String name;
        private java.math.BigDecimal cashBalance;
    }

    @lombok.Getter
    @lombok.Builder
    public static class PortfolioSummaryResponse {
        private Long portfolioId;
        private String name;
        private java.math.BigDecimal cashBalance;
        private java.math.BigDecimal totalEvaluation;
        private java.math.BigDecimal totalInvested;
        private java.math.BigDecimal totalAssets;
        private double returnRate;
        private int holdingCount;
        private boolean profitable;

        public static PortfolioSummaryResponse from(PortfolioSummary summary) {
            return PortfolioSummaryResponse.builder()
                    .portfolioId(summary.getPortfolioId())
                    .name(summary.getName())
                    .cashBalance(summary.getCashBalance().getValue())
                    .totalEvaluation(summary.getTotalEvaluation().getValue())
                    .totalInvested(summary.getTotalInvested().getValue())
                    .totalAssets(summary.getTotalAssets().getValue())
                    .returnRate(summary.getReturnRate())
                    .holdingCount(summary.getHoldingCount())
                    .profitable(summary.isProfitable())
                    .build();
        }
    }
}
