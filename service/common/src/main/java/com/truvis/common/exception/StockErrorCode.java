package com.truvis.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 📈 종목(Stock) 도메인 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum StockErrorCode implements ErrorCode {
    
    // 조회 관련 (404)
    NOT_FOUND("STOCK_001", "종목을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    
    // 중복 관련 (409)
    ALREADY_EXISTS("STOCK_002", "이미 등록된 종목입니다", HttpStatus.CONFLICT),
    
    // 잘못된 데이터 관련 (400)
    INVALID_DATA("STOCK_003", "잘못된 종목 데이터입니다", HttpStatus.BAD_REQUEST),
    INVALID_PRICE("STOCK_004", "유효하지 않은 가격입니다", HttpStatus.BAD_REQUEST),
    
    // Provider 오류 관련 (503)
    PROVIDER_API_CALL_FAILED("STOCK_005", "가격 조회 실패: API 호출 오류", HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.LogLevel.ERROR),
    PROVIDER_NETWORK_ERROR("STOCK_006", "가격 조회 실패: 네트워크 오류", HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.LogLevel.ERROR),
    PROVIDER_RATE_LIMIT("STOCK_007", "가격 조회 실패: API 호출 한도 초과", HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.LogLevel.ERROR),
    PROVIDER_TIMEOUT("STOCK_008", "가격 조회 실패: 타임아웃", HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.LogLevel.ERROR),
    PROVIDER_ERROR("STOCK_009", "가격 조회 실패", HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.LogLevel.ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    private final LogLevel logLevel;
    
    // 기본 로그 레벨은 WARN
    StockErrorCode(String code, String message, HttpStatus httpStatus) {
        this(code, message, httpStatus, LogLevel.WARN);
    }
}

