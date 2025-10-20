package com.truvis.transaction.domain;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    /**
     * 거래 저장
     */
    Transaction save(Transaction transaction);

    /**
     * ID로 거래 조회
     */
    Optional<Transaction> findById(Long id);

    /**
     * 사용자의 모든 거래 조회
     */
    List<Transaction> findByUserId(Long userId);

    /**
     * 사용자의 특정 종목 거래 내역 조회
     */
    List<Transaction> findByUserIdAndStockCode(Long userId, String stockCode);

    /**
     * 거래 삭제
     */
    void delete(Transaction transaction);
}
