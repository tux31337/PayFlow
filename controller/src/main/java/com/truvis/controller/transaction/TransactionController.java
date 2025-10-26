package com.truvis.controller.transaction;

import com.truvis.common.response.ApiResponse;
import com.truvis.transaction.application.TransactionService;
import com.truvis.transaction.domain.Transaction;
import com.truvis.transaction.domain.TransactionType;
import com.truvis.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    /**
     * 거래 실행
     * 인증 필수: JWT에서 사용자 정보를 자동 주입
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> executeTransaction(
            @AuthenticationPrincipal Long userId,  // Spring Security가 자동 주입
            @RequestBody TransactionRequestSecure request
    ) {
        // 거래 실행
        Transaction transaction = transactionService.executeTransaction(
                userId,
                request.stockCode(),
                TransactionType.valueOf(request.type()),
                request.quantity(),
                request.price()
        );

        return ResponseEntity.ok(
                ApiResponse.success(TransactionResponse.from(transaction))
        );
    }

    /**
     * 내 거래 내역 조회
     * 🔒 인증 필수: JWT에서 사용자 정보를 자동 주입
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal Long userId  // 🎯 Spring Security가 자동 주입
    ) {
        List<Transaction> transactions = transactionService.getUserTransactions(userId);
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}

/**
 * 거래 실행 요청 (보안 강화 버전)
 * 🔒 userId 제거: 클라이언트가 임의로 userId를 보낼 수 없음
 */
record TransactionRequestSecure(
        String stockCode,
        String type,  // "BUY" or "SELL"
        int quantity,
        String price
) {}

/**
 * 거래 응답
 */
record TransactionResponse(
        Long id,
        Long userId,
        String stockCode,
        String type,
        String typeName,
        int quantity,
        String price,
        String totalAmount,
        String executedAt
) {
    static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getStockCode().getValue(),
                transaction.getType().name(),
                transaction.getType().getDisplayName(),
                transaction.getQuantity().getValue(),
                transaction.getPrice().getValue().toString(),
                transaction.getTotalAmount().getValue().toString(),
                transaction.getExecutedAt().toString()
        );
    }
}