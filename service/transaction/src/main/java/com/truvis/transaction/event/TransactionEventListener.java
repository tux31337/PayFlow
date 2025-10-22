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
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📢 [이벤트 수신] TransactionCompletedEvent");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  거래 ID: {}", event.getTransactionId());
        log.info("  사용자 ID: {}", event.getUserId());
        log.info("  종목 코드: {}", event.getStockCode());
        log.info("  거래 유형: {} ({})", event.getType(), event.getType().getDisplayName());
        log.info("  수량: {}주", event.getQuantity());
        log.info("  단가: {}원", event.getPrice());
        log.info("  총액: {}원", event.getTotalAmount());
        log.info("  수량 변화: {}", event.getQuantityChange());
        log.info("  실행 시각: {}", event.getExecutedAt());
        log.info("  이벤트 발생 시각: {}", event.getOccurredOn());
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            // 실제 비즈니스 로직 처리

            // 1. Portfolio 업데이트 (나중에 구현)
            if (event.isBuyTransaction()) {
                log.info("📊 [TODO] Portfolio 업데이트: 매수 {}주 추가", event.getQuantity());
                // portfolioService.addHolding(event.getUserId(), event.getStockCode(), event.getQuantity());
            } else {
                log.info("📊 [TODO] Portfolio 업데이트: 매도 {}주 감소", event.getQuantity());
                // portfolioService.removeHolding(event.getUserId(), event.getStockCode(), event.getQuantity());
            }

            // 2. 알림 발송 (나중에 구현)
            log.info("📬 [TODO] 거래 완료 알림 발송: {}", event.getDescription());
            // notificationService.sendTransactionCompleted(event);

            // 3. 분석 데이터 업데이트 (나중에 구현)
            log.info("📈 [TODO] 분석 데이터 업데이트");
            // analyticsService.updateTransactionStats(event);

            log.info("✅ 거래 완료 이벤트 처리 완료");

        } catch (Exception e) {
            log.error("❌ 거래 완료 이벤트 처리 실패: transactionId={}, error={}",
                    event.getTransactionId(), e.getMessage(), e);

            // TODO: 실패 시 재시도 로직 or 데드레터 큐
        }
    }
}