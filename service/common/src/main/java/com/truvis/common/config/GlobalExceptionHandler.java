package com.truvis.common.config;

import com.truvis.common.exception.BusinessException;
import com.truvis.common.exception.EmailVerificationException;
import com.truvis.common.exception.OrderException;
import com.truvis.common.exception.PortfolioException;
import com.truvis.common.exception.StockException;
import com.truvis.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 🌍 글로벌 예외 처리기
 * 
 * 모든 Controller에서 발생하는 예외를 여기서 한 번에 처리해요!
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 */
@RestControllerAdvice  
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 1️⃣ 이메일 인증 관련 예외 처리
     */
    @ExceptionHandler(EmailVerificationException.class)
    public ResponseEntity<ErrorResponse> handleEmailVerificationException(EmailVerificationException e) {
        log.warn("이메일 인증 예외 발생: {}", e.getMessage());
        
        ErrorResponse errorResponse;
        if (e.getMessage().contains("이미 가입된")) {
            errorResponse = ErrorResponse.emailAlreadyExists(e.getMessage().split(": ")[1]);
        } else if (e.getMessage().contains("일치하지 않습니다")) {
            errorResponse = ErrorResponse.invalidVerificationCode();
        } else if (e.getMessage().contains("만료되었습니다")) {
            errorResponse = ErrorResponse.expiredVerificationCode();
        } else {
            errorResponse = ErrorResponse.badRequest(e.getMessage());
        }
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * 2️⃣ 비즈니스 예외 처리 (일반적인 비즈니스 로직 에러)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("비즈니스 예외 발생: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ErrorResponse.badRequest(e.getMessage()));
    }
    
    /**
     * 2-1️⃣ Portfolio 예외 처리
     */
    @ExceptionHandler(PortfolioException.class)
    public ResponseEntity<ErrorResponse> handlePortfolioException(PortfolioException e) {
        String errorCode = e.getErrorCode();
        HttpStatus status;

        // 에러 코드에 따라 HTTP 상태 코드 결정
        if ("PORTFOLIO_001".equals(errorCode)) {
            // 포트폴리오를 찾을 수 없음
            status = HttpStatus.NOT_FOUND;
            log.warn("포트폴리오 조회 실패: {}", e.getMessage());
        } else if ("PORTFOLIO_002".equals(errorCode)) {
            // 이미 존재함
            status = HttpStatus.CONFLICT;
            log.warn("포트폴리오 중복: {}", e.getMessage());
        } else {
            // 그 외 (잔고 부족, 보유 수량 부족 등)
            status = HttpStatus.BAD_REQUEST;
            log.warn("포트폴리오 예외: {}", e.getMessage());
        }

        return ResponseEntity.status(status)
            .body(ErrorResponse.of(e.getMessage(), e.getErrorCode()));
    }

    /**
     * 2-2️⃣ Order 예외 처리
     */
    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(OrderException e) {
        String errorCode = e.getErrorCode();
        HttpStatus status;

        // 에러 코드에 따라 HTTP 상태 코드 결정
        if ("ORDER_001".equals(errorCode)) {
            // 주문을 찾을 수 없음
            status = HttpStatus.NOT_FOUND;
            log.warn("주문 조회 실패: {}", e.getMessage());
        } else if ("ORDER_002".equals(errorCode)) {
            // 권한 없음
            status = HttpStatus.FORBIDDEN;
            log.warn("주문 접근 권한 없음: {}", e.getMessage());
        } else {
            // 그 외 (잔고 부족, 보유 수량 부족, 파라미터 오류 등)
            status = HttpStatus.BAD_REQUEST;
            log.warn("주문 예외: {}", e.getMessage());
        }

        return ResponseEntity.status(status)
            .body(ErrorResponse.of(e.getMessage(), e.getErrorCode()));
    }

    /**
     * 2-3️⃣ Stock 예외 처리 (에러 코드별 HTTP 상태 매핑)
     */
    @ExceptionHandler(StockException.class)
    public ResponseEntity<ErrorResponse> handleStockException(StockException e) {
        String errorCode = e.getErrorCode();
        HttpStatus status;
        
        // 에러 코드에 따라 HTTP 상태 코드 결정
        if ("STOCK_001".equals(errorCode)) {
            // 종목을 찾을 수 없음
            status = HttpStatus.NOT_FOUND;
            log.warn("종목 조회 실패: {}", e.getMessage());
        } else if ("STOCK_002".equals(errorCode)) {
            // 종목이 이미 존재함
            status = HttpStatus.CONFLICT;
            log.warn("종목 중복 등록 시도: {}", e.getMessage());
        } else if (errorCode.startsWith("STOCK_00") && 
                   (errorCode.equals("STOCK_003") || errorCode.equals("STOCK_004"))) {
            // 잘못된 데이터 (STOCK_003, STOCK_004)
            status = HttpStatus.BAD_REQUEST;
            log.warn("잘못된 종목 데이터: {}", e.getMessage());
        } else if (errorCode.startsWith("STOCK_00") && 
                   Integer.parseInt(errorCode.substring(6)) >= 5) {
            // Provider 오류 (STOCK_005 ~ STOCK_009)
            status = HttpStatus.SERVICE_UNAVAILABLE;
            log.error("주가 제공자 오류: {}", e.getMessage(), e);
        } else {
            // 기본값
            status = HttpStatus.BAD_REQUEST;
            log.warn("Stock 예외: {}", e.getMessage());
        }
        
        return ResponseEntity.status(status)
            .body(ErrorResponse.of(e.getMessage(), e.getErrorCode()));
    }
    
    /**
     * 3️⃣ Validation 예외 처리 (@Valid 어노테이션에서 발생)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        
        // 모든 필드 에러를 하나의 메시지로 합치기
        String message = bindingResult.getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        log.warn("Validation 예외 발생: {}", message);
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of(message, "VALIDATION_ERROR"));
    }
    
    /**
     * 4️⃣ IllegalArgumentException 처리 (잘못된 파라미터)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 파라미터: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ErrorResponse.badRequest(e.getMessage()));
    }

    /**
     * 4-1️⃣ IllegalStateException 처리 (잘못된 상태)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.warn("잘못된 상태: {}", e.getMessage());
        
        // 인증 관련 메시지인 경우 401 반환
        if (e.getMessage() != null && e.getMessage().contains("인증")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(e.getMessage(), "AUTH_REQUIRED"));
        }
        
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of(e.getMessage(), "INVALID_STATE"));
    }
    
    /**
     * 5️⃣ 예상하지 못한 모든 예외 처리 (최후의 보루)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("예상하지 못한 에러 발생", e);  // 스택트레이스까지 로깅
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.internalServerError("서버 내부 오류가 발생했습니다"));
    }
}
