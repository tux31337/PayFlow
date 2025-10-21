package com.truvis.transaction.application;

import com.truvis.transaction.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 거래 Application Service
 * - 거래 실행, 조회 등의 유스케이스 처리
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * 거래 실행
     *
     * @return 실행된 거래
     */
    @Transactional
    public Transaction executeTransaction(
            Long userId,
            String stockCode,
            TransactionType type,
            int quantity,
            String price
    ) {
        log.info("🔄 거래 실행 시작: userId={}, stockCode={}, type={}, quantity={}, price={}",
                userId, stockCode, type.getDisplayName(), quantity, price);

        // 1. 도메인 객체 생성 (비즈니스 규칙 검증 포함)
        Transaction transaction = Transaction.execute(
                userId,
                StockCode.of(stockCode),
                type,
                Quantity.of(quantity),
                Price.of(price)
        );

        log.debug("거래 객체 생성 완료: {}", transaction.getDescription());

        // 2. 저장 (자동으로 도메인 이벤트 발행!)
        Transaction saved = transactionRepository.save(transaction);

        log.info("✅ 거래 실행 완료: id={}, totalAmount={}원",
                saved.getId(), saved.getTotalAmount().getValue());

        return saved;
    }

    /**
     * 거래 ID로 조회
     */
    public Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "거래를 찾을 수 없습니다: " + transactionId
                ));
    }

    /**
     * 사용자의 모든 거래 조회
     */
    public List<Transaction> getUserTransactions(Long userId) {
        log.info("📋 사용자 거래 내역 조회: userId={}", userId);
        return transactionRepository.findByUserId(userId);
    }

    /**
     * 사용자의 특정 종목 거래 내역 조회
     */
    public List<Transaction> getUserStockTransactions(Long userId, String stockCode) {
        log.info("📋 사용자 종목별 거래 내역 조회: userId={}, stockCode={}",
                userId, stockCode);
        return transactionRepository.findByUserIdAndStockCode(userId, stockCode);
    }
}