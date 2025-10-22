package com.truvis.controller.transaction;

import com.truvis.common.response.ApiResponse;
import com.truvis.transaction.application.TransactionService;
import com.truvis.transaction.domain.Transaction;
import com.truvis.transaction.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 거래 실행
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> executeTransaction(
            @RequestBody TransactionRequest request
    ) {
        log.info("🔵 [API] 거래 실행 요청: {}", request);

        Transaction transaction = transactionService.executeTransaction(
                request.userId(),
                request.stockCode(),
                TransactionType.valueOf(request.type()),
                request.quantity(),
                request.price()
        );

        log.info("🔵 [API] 거래 실행 완료: id={}", transaction.getId());

        return ResponseEntity.ok(
                ApiResponse.success(TransactionResponse.from(transaction))
        );
    }

    /**
     * 사용자 거래 내역 조회
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getUserTransactions(
            @PathVariable Long userId
    ) {
        log.info("🔵 [API] 사용자 거래 내역 조회: userId={}", userId);

        List<Transaction> transactions = transactionService.getUserTransactions(userId);
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}

/**
 * 거래 실행 요청
 */
record TransactionRequest(
        Long userId,
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