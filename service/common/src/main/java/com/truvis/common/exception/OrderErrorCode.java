package com.truvis.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 📋 주문(Order) 도메인 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    
    // 조회 관련 (404)
    NOT_FOUND("ORDER_001", "주문을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    
    // 권한 관련 (403)
    UNAUTHORIZED("ORDER_002", "본인의 주문만 접근할 수 있습니다", HttpStatus.FORBIDDEN),
    
    // 비즈니스 로직 관련 (400)
    CANNOT_CANCEL("ORDER_003", "취소할 수 없는 주문 상태입니다", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE("ORDER_004", "잔고가 부족하여 매수 주문을 생성할 수 없습니다", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_HOLDING("ORDER_005", "보유 수량이 부족하여 매도 주문을 생성할 수 없습니다", HttpStatus.BAD_REQUEST),
    LIMIT_PRICE_REQUIRED("ORDER_006", "지정가 주문은 지정가가 필요합니다", HttpStatus.BAD_REQUEST),
    ESTIMATED_PRICE_REQUIRED("ORDER_007", "시장가 매수 주문은 예상 가격이 필요합니다", HttpStatus.BAD_REQUEST),
    ALREADY_FILLED("ORDER_008", "이미 체결 완료된 주문입니다", HttpStatus.BAD_REQUEST);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

