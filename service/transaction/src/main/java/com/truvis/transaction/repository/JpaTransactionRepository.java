package com.truvis.transaction.repository;

import com.truvis.transaction.domain.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository 인터페이스
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
 * Transaction Repository JPA 구현체
 * - JPA 기술로 구현
 * - 영속성 처리 담당
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;

    /**
     * 거래 저장
     */
    @Override
    public Transaction save(Transaction transaction) {
        Transaction saved = jpaRepository.save(transaction);
        
        log.debug("거래 저장: id={}, type={}", 
                saved.getId(), 
                saved.getType().getDisplayName());
        
        return saved;
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
