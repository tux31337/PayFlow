package com.truvis.stock.infrastructure.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * SSE Emitter 관리자
 * - 종목별 SSE 연결 관리
 * - 클라이언트에게 실시간 데이터 전송
 */
@Slf4j
@Component
public class SseEmitterManager {
    
    /**
     * SSE 타임아웃 (30분)
     */
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;
    
    /**
     * 종목별 SSE Emitter 목록
     * Key: 종목코드, Value: Emitter 집합
     */
    private final Map<String, CopyOnWriteArraySet<SseEmitter>> emitters = new ConcurrentHashMap<>();
    
    /**
     * SSE Emitter 생성 및 등록
     */
    public SseEmitter createEmitter(String stockCode) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        // 종목별 Emitter 집합 가져오기 (없으면 생성)
        CopyOnWriteArraySet<SseEmitter> stockEmitters = emitters.computeIfAbsent(
                stockCode,
                k -> new CopyOnWriteArraySet<>()
        );
        
        // Emitter 추가
        stockEmitters.add(emitter);
        
        log.info("[SSE] Emitter 생성: 종목={}, 총 연결={}개", stockCode, stockEmitters.size());
        
        // 완료/타임아웃/에러 시 제거
        emitter.onCompletion(() -> removeEmitter(stockCode, emitter));
        emitter.onTimeout(() -> removeEmitter(stockCode, emitter));
        emitter.onError(e -> removeEmitter(stockCode, emitter));
        
        // 연결 확인 메시지 전송
        sendConnectedMessage(emitter, stockCode);
        
        return emitter;
    }
    
    /**
     * 연결 확인 메시지 전송
     */
    private void sendConnectedMessage(SseEmitter emitter, String stockCode) {
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to " + stockCode + " price stream"));
        } catch (IOException e) {
            log.error("[SSE] 연결 메시지 전송 실패: {}", e.getMessage());
        }
    }
    
    /**
     * Emitter 제거
     */
    private void removeEmitter(String stockCode, SseEmitter emitter) {
        CopyOnWriteArraySet<SseEmitter> stockEmitters = emitters.get(stockCode);
        if (stockEmitters != null) {
            stockEmitters.remove(emitter);
            log.info("[SSE] Emitter 제거: 종목={}, 남은 연결={}개", stockCode, stockEmitters.size());
            
            // 해당 종목의 모든 연결이 끊어지면 Map에서 제거
            if (stockEmitters.isEmpty()) {
                emitters.remove(stockCode);
                log.info("[SSE] 종목 구독 종료: {}", stockCode);
            }
        }
    }
    
    /**
     * 특정 종목의 모든 구독자에게 데이터 전송
     */
    public void sendToStock(String stockCode, Object data) {
        CopyOnWriteArraySet<SseEmitter> stockEmitters = emitters.get(stockCode);
        
        if (stockEmitters == null || stockEmitters.isEmpty()) {
            return;
        }
        
        // 전송 실패한 Emitter 목록
        CopyOnWriteArraySet<SseEmitter> deadEmitters = new CopyOnWriteArraySet<>();
        
        for (SseEmitter emitter : stockEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("price-update")
                        .data(data));
            } catch (Exception e) {
                log.warn("[SSE] 데이터 전송 실패: 종목={}, 오류={}", stockCode, e.getMessage());
                deadEmitters.add(emitter);
            }
        }
        
        // 실패한 Emitter 제거
        deadEmitters.forEach(emitter -> removeEmitter(stockCode, emitter));
    }
    
    /**
     * 특정 종목의 구독자 수 조회
     */
    public int getSubscriberCount(String stockCode) {
        CopyOnWriteArraySet<SseEmitter> stockEmitters = emitters.get(stockCode);
        return stockEmitters != null ? stockEmitters.size() : 0;
    }
    
    /**
     * 전체 구독 종목 수 조회
     */
    public int getSubscribedStockCount() {
        return emitters.size();
    }
}
