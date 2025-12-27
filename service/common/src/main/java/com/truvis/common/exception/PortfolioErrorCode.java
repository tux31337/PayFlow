package com.truvis.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 💼 포트폴리오(Portfolio) 도메인 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum PortfolioErrorCode implements ErrorCode {
    
    // 조회 관련 (404)
    NOT_FOUND("PORTFOLIO_001", "포트폴리오를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    
    // 중복 관련 (409)
    ALREADY_EXISTS("PORTFOLIO_002", "이미 포트폴리오가 존재합니다", HttpStatus.CONFLICT),
    
    // 비즈니스 로직 관련 (400)
    INSUFFICIENT_BALANCE("PORTFOLIO_003", "잔고가 부족합니다", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_HOLDING("PORTFOLIO_004", "보유 수량이 부족합니다", HttpStatus.BAD_REQUEST),
    STOCK_NOT_HELD("PORTFOLIO_005", "보유하지 않은 종목입니다", HttpStatus.BAD_REQUEST),
    MAX_HOLDINGS_EXCEEDED("PORTFOLIO_006", "최대 보유 종목 수를 초과했습니다", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_WITH_HOLDINGS("PORTFOLIO_007", "보유 종목이 있는 포트폴리오는 삭제할 수 없습니다", HttpStatus.BAD_REQUEST);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

