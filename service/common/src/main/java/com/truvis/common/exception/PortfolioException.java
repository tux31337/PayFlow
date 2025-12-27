package com.truvis.common.exception;

/**
 * Portfolio 도메인 예외
 */
public class PortfolioException extends BusinessException {

    public PortfolioException(String errorCode, String message) {
        super(errorCode, message);
    }

    public PortfolioException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    // ==================== 팩토리 메서드 ====================

    /**
     * 포트폴리오를 찾을 수 없음
     */
    public static PortfolioException notFound(Long userId) {
        return new PortfolioException(
                "PORTFOLIO_001",
                String.format("포트폴리오를 찾을 수 없습니다. userId: %d", userId)
        );
    }

    /**
     * 포트폴리오가 이미 존재함
     */
    public static PortfolioException alreadyExists(Long userId) {
        return new PortfolioException(
                "PORTFOLIO_002",
                String.format("이미 포트폴리오가 존재합니다. userId: %d", userId)
        );
    }

    /**
     * 잔고 부족
     */
    public static PortfolioException insufficientBalance(long currentBalance, long requiredAmount) {
        return new PortfolioException(
                "PORTFOLIO_003",
                String.format("잔고가 부족합니다. 현재 잔고: %,d원, 필요 금액: %,d원", currentBalance, requiredAmount)
        );
    }

    /**
     * 보유 수량 부족
     */
    public static PortfolioException insufficientHolding(String stockCode, int holdingQty, int requestedQty) {
        return new PortfolioException(
                "PORTFOLIO_004",
                String.format("보유 수량이 부족합니다. 종목: %s, 보유: %d주, 요청: %d주", stockCode, holdingQty, requestedQty)
        );
    }

    /**
     * 종목 미보유
     */
    public static PortfolioException stockNotHeld(String stockCode) {
        return new PortfolioException(
                "PORTFOLIO_005",
                String.format("보유하지 않은 종목입니다: %s", stockCode)
        );
    }

    /**
     * 최대 보유 종목 수 초과
     */
    public static PortfolioException maxHoldingsExceeded(int maxHoldings) {
        return new PortfolioException(
                "PORTFOLIO_006",
                String.format("포트폴리오는 최대 %d개 종목까지만 보유할 수 있습니다", maxHoldings)
        );
    }

    /**
     * 보유 종목이 있어 삭제 불가
     */
    public static PortfolioException cannotDeleteWithHoldings() {
        return new PortfolioException(
                "PORTFOLIO_007",
                "보유 종목이 있는 포트폴리오는 삭제할 수 없습니다"
        );
    }
}

