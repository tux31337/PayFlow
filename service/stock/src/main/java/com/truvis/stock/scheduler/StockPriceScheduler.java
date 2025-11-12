package com.truvis.stock.scheduler;

import com.truvis.stock.application.StockApplicationService;
import com.truvis.stock.infrastructure.websocket.KisWebSocketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Stock 관리 스케줄러
 * 
 * - WebSocket 헬스체크 및 재연결
 * - 장 마감 후 최종 가격 보정
 */
@Slf4j
@Component
@Profile({"local", "prod"})
@RequiredArgsConstructor
public class StockPriceScheduler {

    private final StockApplicationService stockApplicationService;
    private final KisWebSocketClient webSocketClient;

    /**
     * WebSocket 헬스 모니터링
     * - 1분마다 연결 상태 확인
     * - 연결 끊김 시 자동 재연결
     */
    @Scheduled(fixedDelay = 60000)  // 1분
    public void monitorWebSocketHealth() {
        if (!webSocketClient.isHealthy()) {
            log.warn("⚠️  [헬스체크] WebSocket 상태 불량 감지");
            
            // 재연결 시도
            webSocketClient.reconnect();
            
            // 재연결 실패 시 REST API로 백업
            if (!webSocketClient.isHealthy()) {
                log.error("❌ [헬스체크] WebSocket 재연결 실패, REST API로 임시 대체");
                try {
                    stockApplicationService.updateAllStockPrices();
                } catch (Exception e) {
                    log.error("❌ [헬스체크] REST API 백업도 실패: {}", e.getMessage());
                }
            } else {
                log.info("✅ [헬스체크] WebSocket 재연결 성공");
            }
        } else {
            log.debug("✅ [헬스체크] WebSocket 정상 작동 중");
        }
    }

    /**
     * 장 마감 후 최종 가격 보정
     * - 주중 15:35 실행 (장 마감 5분 후)
     * - WebSocket 데이터와 REST 데이터 검증용
     */
    @Scheduled(cron = "0 35 15 * * MON-FRI", zone = "Asia/Seoul")
    public void updateFinalPrices() {
        log.info("🔔 [스케줄러] 장 마감 후 최종 가격 보정 시작");
        
        try {
            stockApplicationService.updateAllStockPrices();
            log.info("✅ [스케줄러] 장 마감 후 가격 보정 완료");
        } catch (Exception e) {
            log.error("❌ [스케줄러] 장 마감 후 가격 보정 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 장 시작 전 가격 업데이트
     * - 주중 08:50 실행 (장 시작 10분 전)
     * - 전일 종가 확인용
     */
    @Scheduled(cron = "0 50 8 * * MON-FRI", zone = "Asia/Seoul")
    public void updatePreMarketPrices() {
        log.info("🌅 [스케줄러] 장 시작 전 가격 업데이트 시작");
        
        try {
            stockApplicationService.updateAllStockPrices();
            log.info("✅ [스케줄러] 장 시작 전 가격 업데이트 완료");
        } catch (Exception e) {
            log.error("❌ [스케줄러] 장 시작 전 가격 업데이트 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * Redis → DB 가격 동기화
     * - 1분마다 실행
     * - WebSocket으로 받은 실시간 가격을 DB에 반영
     * - DB 부하 최소화를 위해 배치로 처리
     */
    @Scheduled(fixedDelay = 60000)  // 1분
    public void syncPricesFromRedis() {
        log.debug("🔄 [스케줄러] Redis → DB 가격 동기화 시작");
        
        try {
            stockApplicationService.syncPricesFromRedisToDatabase();
        } catch (Exception e) {
            log.error("❌ [스케줄러] Redis → DB 동기화 실패: {}", e.getMessage(), e);
        }
    }
}
