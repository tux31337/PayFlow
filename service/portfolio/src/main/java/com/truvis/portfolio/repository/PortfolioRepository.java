package com.truvis.portfolio.repository;

import com.truvis.portfolio.domain.Portfolio;

import java.util.List;
import java.util.Optional;

/**
 * Portfolio Repository 인터페이스
 */
public interface PortfolioRepository {

    /**
     * 포트폴리오 저장
     */
    Portfolio save(Portfolio portfolio);

    /**
     * ID로 포트폴리오 조회
     */
    Optional<Portfolio> findById(Long id);

    /**
     * 사용자 ID로 포트폴리오 조회
     */
    Optional<Portfolio> findByUserId(Long userId);

    /**
     * 사용자의 모든 포트폴리오 조회 (여러 개 가능)
     */
    List<Portfolio> findAllByUserId(Long userId);

    /**
     * 포트폴리오 삭제
     */
    void delete(Portfolio portfolio);

    /**
     * 모든 포트폴리오 삭제 (테스트용)
     */
    void deleteAll();
}
