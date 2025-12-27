package com.truvis.order.repository;

import com.truvis.common.model.vo.StockCode;
import com.truvis.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * JPA Order Repository 구현
 */
public interface JpaOrderRepository extends OrderRepository, JpaRepository<Order, Long> {

    /**
     * 사용자의 모든 주문 조회
     */
    @Override
    List<Order> findByUserId(Long userId);

    /**
     * 사용자의 활성 주문 조회
     */
    @Override
    @Query("SELECT o FROM Order o " +
           "WHERE o.userId = :userId " +
           "AND o.status IN ('PENDING', 'PARTIALLY_FILLED') " +
           "ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByUserId(@Param("userId") Long userId);

    /**
     * 종목의 모든 활성 주문 조회
     */
    @Override
    @Query("SELECT o FROM Order o " +
           "WHERE o.stockCode = :stockCode " +
           "AND o.status IN ('PENDING', 'PARTIALLY_FILLED') " +
           "ORDER BY o.createdAt ASC")
    List<Order> findActiveOrdersByStockCode(@Param("stockCode") StockCode stockCode);

    /**
     * 사용자의 특정 종목에 대한 활성 주문 조회
     */
    @Override
    @Query("SELECT o FROM Order o " +
           "WHERE o.userId = :userId " +
           "AND o.stockCode = :stockCode " +
           "AND o.status IN ('PENDING', 'PARTIALLY_FILLED') " +
           "ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByUserIdAndStockCode(
            @Param("userId") Long userId,
            @Param("stockCode") StockCode stockCode
    );

    /**
     * 모든 활성 주문 조회 (매칭 엔진용)
     */
    @Override
    @Query("SELECT o FROM Order o " +
           "WHERE o.status IN ('PENDING', 'PARTIALLY_FILLED') " +
           "ORDER BY o.createdAt ASC")
    List<Order> findAllActiveOrders();
}
