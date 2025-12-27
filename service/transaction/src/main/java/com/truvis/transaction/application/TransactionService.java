package com.truvis.transaction.application;

import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.common.model.vo.StockCode;
import com.truvis.transaction.domain.Transaction;
import com.truvis.transaction.domain.TransactionType;
import com.truvis.transaction.event.TransactionCompletedEvent;
import com.truvis.transaction.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 거래 실행
     * - 거래 생성 → 저장 → 이벤트 발행
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
        // 1. 도메인 객체 생성 (비즈니스 규칙 검증 포함)
        Transaction transaction = Transaction.create(
                userId,
                StockCode.of(stockCode),
                type,
                Quantity.of(quantity),
                Price.of(price)
        );

        log.debug("거래 객체 생성: {}", transaction.getDescription());

        // 2. 저장
        Transaction saved = transactionRepository.save(transaction);

        log.info("거래 저장 완료: id={}, type={}, amount={}원",
                saved.getId(),
                type.getDisplayName(),
                saved.getTotalAmount().getValue());

        // 3. 🎯 도메인 이벤트 발행 (명시적!)
        eventPublisher.publishEvent(
                TransactionCompletedEvent.of(saved)
        );

        log.debug("거래 완료 이벤트 발행: transactionId={}", saved.getId());

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