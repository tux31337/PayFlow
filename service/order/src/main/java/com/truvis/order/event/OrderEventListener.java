package com.truvis.order.event;

import com.truvis.common.model.vo.Price;
import com.truvis.common.model.vo.Quantity;
import com.truvis.order.domain.OrderSide;
import com.truvis.portfolio.domain.Portfolio;
import com.truvis.portfolio.repository.PortfolioRepository;
import com.truvis.transaction.domain.Transaction;
import com.truvis.transaction.domain.TransactionRepository;
import com.truvis.transaction.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order 도메인 이벤트 리스너
 * 
 * 주문 체결 시 자동으로:
 * 1. Transaction 생성 (거래 기록)
 * 2. Portfolio 업데이트 (잔고/보유 수량 변경)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;

    /**
     * 주문 체결 이벤트 처리
     * 
     * OrderService.fillOrder() 호출 시 발행된 이벤트를 처리
     */
    @EventListener
    @Transactional
    public void onOrderFilled(OrderFilledEvent event) {
        log.info("주문 체결 이벤트 수신 - orderId: {}, userId: {}, stockCode: {}, side: {}, quantity: {}, price: {}",
                event.orderId(),
                event.userId(),
                event.stockCode().getValue(),
                event.side(),
                event.filledQuantity(),
                event.filledPrice());

        try {
            // 1. Transaction 생성 (거래 기록)
            createTransaction(event);

            // 2. Portfolio 업데이트 (잔고/보유 수량)
            updatePortfolio(event);

            log.info("주문 체결 이벤트 처리 완료 - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("주문 체결 이벤트 처리 실패 - orderId: {}, error: {}", 
                    event.orderId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Transaction 생성
     */
    private void createTransaction(OrderFilledEvent event) {
        // OrderSide를 TransactionType으로 변환
        TransactionType transactionType = event.side() == OrderSide.BUY
                ? TransactionType.BUY
                : TransactionType.SELL;

        Transaction transaction = Transaction.create(
                event.userId(),
                event.stockCode(),
                transactionType,
                Quantity.of(event.filledQuantity()),
                Price.of(event.filledPrice())
        );

        transactionRepository.save(transaction);

        log.info("Transaction 생성 완료 - userId: {}, type: {}, stockCode: {}, quantity: {}, price: {}",
                event.userId(),
                transactionType,
                event.stockCode().getValue(),
                event.filledQuantity(),
                event.filledPrice());
    }

    /**
     * Portfolio 업데이트
     */
    private void updatePortfolio(OrderFilledEvent event) {
        // 사용자의 포트폴리오 조회
        Portfolio portfolio = portfolioRepository.findByUserId(event.userId())
                .orElseThrow(() -> new IllegalStateException(
                        String.format("포트폴리오를 찾을 수 없습니다. userId: %d", event.userId())
                ));

        // 매수/매도에 따라 포트폴리오 업데이트
        if (event.side() == OrderSide.BUY) {
            // 매수: 종목 추가 (잔고 차감은 Portfolio 내부에서 처리)
            portfolio.buyStock(
                    event.stockCode(),
                    Quantity.of(event.filledQuantity()),
                    Price.of(event.filledPrice())
            );

            log.info("Portfolio 매수 업데이트 완료 - userId: {}, stockCode: {}, quantity: {}, price: {}",
                    event.userId(),
                    event.stockCode().getValue(),
                    event.filledQuantity(),
                    event.filledPrice());
        } else {
            // 매도: 종목 감소 (현금 증가는 Portfolio 내부에서 처리)
            portfolio.sellStock(
                    event.stockCode(),
                    Quantity.of(event.filledQuantity()),
                    Price.of(event.filledPrice())
            );

            log.info("Portfolio 매도 업데이트 완료 - userId: {}, stockCode: {}, quantity: {}, price: {}",
                    event.userId(),
                    event.stockCode().getValue(),
                    event.filledQuantity(),
                    event.filledPrice());
        }

        portfolioRepository.save(portfolio);
    }

    /**
     * 주문 생성 이벤트 처리 (선택적)
     * 
     * 필요시 주문 생성 시 알림 발송 등의 처리 가능
     */
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신 - orderId: {}, userId: {}, stockCode: {}, side: {}, type: {}",
                event.orderId(),
                event.userId(),
                event.stockCode().getValue(),
                event.side(),
                event.type());

        // TODO: 필요시 알림 발송, 로깅 등 추가 처리
    }

    /**
     * 주문 취소 이벤트 처리 (선택적)
     */
    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("주문 취소 이벤트 수신 - orderId: {}, userId: {}, reason: {}",
                event.orderId(),
                event.userId(),
                event.reason());

        // TODO: 필요시 알림 발송, 로깅 등 추가 처리
    }
}
