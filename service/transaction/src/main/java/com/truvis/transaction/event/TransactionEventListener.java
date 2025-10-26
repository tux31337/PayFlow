package com.truvis.transaction.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Transaction 도메인 이벤트 리스너
 * - TransactionCompletedEvent를 수신하여 처리
 * - 비동기 처리로 Transaction 저장과 분리
 */
@Component
@Slf4j
public class TransactionEventListener {

    /**
     * 거래 완료 이벤트 처리
     *
     * @Async: 비동기 처리 (별도 스레드에서 실행)
     * @EventListener: Spring Events 자동 감지
     */
    @Async("transactionExecutor")
    @EventListener
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        log.info("거래 완료 이벤트 수신: transactionId={}, type={}, amount={}",
                event.getTransactionId(), 
                event.getType().getDisplayName(),
                event.getTotalAmount());

        if (log.isDebugEnabled()) {
            log.debug("거래 상세: stockCode={}, quantity={}, price={}, executedAt={}",
                    event.getStockCode(), event.getQuantity(), event.getPrice(), event.getExecutedAt());
        }

        try {
            // TODO: Portfolio 업데이트
            // portfolioService.updateHolding(event);

            // TODO: 알림 발송
            // notificationService.sendTransactionCompleted(event);

            // TODO: 분석 데이터 업데이트
            // analyticsService.updateTransactionStats(event);

            log.debug("거래 완료 이벤트 처리 완료: transactionId={}", event.getTransactionId());

        } catch (Exception e) {
            log.error("거래 완료 이벤트 처리 실패: transactionId={}, error={}",
                    event.getTransactionId(), e.getMessage(), e);
            // TODO: 실패 시 재시도 로직 or 데드레터 큐
        }
    }
}