package com.truvis.order.repository;

import com.truvis.common.model.vo.StockCode;
import com.truvis.order.domain.Order;

import java.util.List;
import java.util.Optional;

/**
 * Order Repository 인터페이스
 * 
 * 도메인 계층의 추상화 - 구현은 인프라 계층에서
 */
public interface OrderRepository {

    /**
     * 주문 저장
     */
    Order save(Order order);

    /**
     * ID로 주문 조회
     */
    Optional<Order> findById(Long id);

    /**
     * 사용자의 모든 주문 조회
     */
    List<Order> findByUserId(Long userId);

    /**
     * 사용자의 활성 주문 조회 (PENDING, PARTIALLY_FILLED)
     */
    List<Order> findActiveOrdersByUserId(Long userId);

    /**
     * 종목의 모든 활성 주문 조회
     */
    List<Order> findActiveOrdersByStockCode(StockCode stockCode);

    /**
     * 사용자의 특정 종목에 대한 활성 주문 조회
     */
    List<Order> findActiveOrdersByUserIdAndStockCode(Long userId, StockCode stockCode);

    /**
     * 모든 활성 주문 조회 (매칭 엔진용)
     */
    List<Order> findAllActiveOrders();

    /**
     * 주문 삭제 (테스트용)
     */
    void delete(Order order);

    /**
     * 모든 주문 삭제 (테스트용)
     */
    void deleteAll();
}
