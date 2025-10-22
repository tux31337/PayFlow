package com.truvis.transaction.infrastructure;

import com.truvis.common.model.DomainEvent;
import com.truvis.transaction.domain.Transaction;
import com.truvis.transaction.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository 인터페이스
 * - Spring Data JPA가 자동으로 구현체 생성
 */
interface TransactionJpaRepository extends JpaRepository<Transaction, Long> {

    /**
     * 사용자 ID로 거래 조회
     */
    List<Transaction> findByUserId(Long userId);

    /**
     * 사용자 ID와 종목 코드로 거래 조회
     * - stockCode는 @Embeddable이라 .value로 접근
     */
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.stockCode.value = :stockCode")
    List<Transaction> findByUserIdAndStockCode(
            @Param("userId") Long userId,
            @Param("stockCode") String stockCode
    );
}

/**
 * Transaction Repository 구현체 (Adapter)
 * - JPA 기술 구현
 * - 도메인 이벤트 자동 발행 (🎯 핵심!)
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class JpaTransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;  // 이벤트 발행기

    /**
     * 거래 저장 + 도메인 이벤트 발행
     */
    @Override
    public Transaction save(Transaction transaction) {
        // 1. DB 저장
        Transaction savedTransaction = jpaRepository.save(transaction);

        // 2. 🎯 도메인 이벤트 발행!
        List<DomainEvent> events = savedTransaction.getDomainEvents();

        if (!events.isEmpty()) {
            log.info("📢 도메인 이벤트 발행 시작: {} 개", events.size());

            events.forEach(event -> {
                log.info("  → 발행: {}", event.getClass().getSimpleName());
                eventPublisher.publishEvent(event);
            });

            // 3. 이벤트 클리어
            savedTransaction.clearDomainEvents();

            log.info("✅ 도메인 이벤트 발행 완료");
        }

        return savedTransaction;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Transaction> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<Transaction> findByUserIdAndStockCode(Long userId, String stockCode) {
        return jpaRepository.findByUserIdAndStockCode(userId, stockCode);
    }

    @Override
    public void delete(Transaction transaction) {
        jpaRepository.delete(transaction);
    }
}