package com.truvis.portfolio.repository;

import com.truvis.portfolio.domain.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * JPA Portfolio Repository 구현
 */
public interface JpaPortfolioRepository extends PortfolioRepository, JpaRepository<Portfolio, Long> {

    /**
     * 사용자 ID로 포트폴리오 조회
     */
    @Override
    Optional<Portfolio> findByUserId(Long userId);

    /**
     * 사용자의 모든 포트폴리오 조회
     */
    @Override
    @Query("SELECT p FROM Portfolio p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    List<Portfolio> findAllByUserId(@Param("userId") Long userId);
}
