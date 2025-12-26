package com.truvis.order.infrastructure;

import com.truvis.common.model.vo.Price;
import com.truvis.order.application.OrderService;
import com.truvis.order.domain.Order;
import com.truvis.stock.application.StockApplicationService;
import com.truvis.stock.model.StockDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Order Matching Engine (주문 체결 엔진)
 * 
 * 실시간 시장 가격을 모니터링하여 주문 체결 조건을 확인하고 자동으로 체결 처리
 * 
 * 동작 방식:
 * 1. 1초마다 실행 (스케줄러)
 * 2. 모든 활성 주문 조회 (PENDING, PARTIALLY_FILLED)
 * 3. 각 주문의 종목 현재가 조회 (Stock 모듈 연동)
 * 4. 체결 조건 충족 시 fillOrder() 호출
 * 
 * 실제 거래소와 동일한 방식:
 * - 지정가 매수: 현재가 <= 지정가일 때 체결
 * - 지정가 매도: 현재가 >= 지정가일 때 체결
 * - 시장가: 즉시 체결
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMatchingEngine {

    private final OrderService orderService;
    private final StockApplicationService stockService;

    /**
     * 주문 매칭 스케줄러
     * 
     * 1초마다 실행되어 체결 가능한 주문을 찾아 체결 처리
     */
    @Scheduled(fixedDelay = 1000) // 1초마다
    public void matchOrders() {
        try {
            // 1. 모든 활성 주문 조회
            List<Order> activeOrders = orderService.getAllActiveOrders();
            
            if (activeOrders.isEmpty()) {
                return; // 활성 주문 없음
            }

            log.debug("주문 매칭 시작 - 활성 주문 {}개", activeOrders.size());

            // 2. 각 주문에 대해 체결 조건 확인
            for (Order order : activeOrders) {
                try {
                    matchOrder(order);
                } catch (Exception e) {
                    log.error("주문 매칭 실패 - orderId: {}, error: {}", 
                            order.getId(), e.getMessage(), e);
                }
            }

            log.debug("주문 매칭 완료");

        } catch (Exception e) {
            log.error("주문 매칭 엔진 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 개별 주문 매칭
     */
    private void matchOrder(Order order) {
        try {
            // Stock 모듈에서 실시간 가격 조회
            StockDetailResponse stock = stockService.getStockDetail(
                    order.getStockCode().getValue()
            );
            
            // 가격 파싱 ("71,000" → 71000L)
            Price currentPrice = parsePrice(stock.getCurrentPrice());  
            
            // 체결 조건 확인
            if (order.canFillAtPrice(currentPrice)) {
                log.info("체결 조건 충족! orderId: {}, stockCode: {}, orderType: {}, limitPrice: {}, currentPrice: {}",
                        order.getId(),
                        order.getStockCode().getValue(),
                        order.getType(),
                        order.getLimitPrice() != null ? order.getLimitPrice().getValue() : "N/A",
                        currentPrice.getValue());

                // 주문 체결 (전체 수량 체결)
                orderService.fillOrder(
                        order.getId(),
                        order.getRemainingQuantity(),
                        currentPrice
                );

                log.info("주문 체결 완료 - orderId: {}", order.getId());
            }
        } catch (Exception e) {
            log.error("주문 매칭 중 오류 - orderId: {}, stockCode: {}, error: {}", 
                    order.getId(), 
                    order.getStockCode().getValue(), 
                    e.getMessage());
        }
    }

    /**
     * 가격 문자열 파싱
     * "71,000" → 71000L
     */
    private Price parsePrice(String priceStr) {
        String cleaned = priceStr.replace(",", "");
        return Price.of(Long.parseLong(cleaned));
    }
}
