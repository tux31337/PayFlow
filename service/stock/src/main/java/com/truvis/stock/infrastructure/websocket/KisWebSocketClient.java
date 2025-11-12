package com.truvis.stock.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truvis.common.model.vo.StockCode;
import com.truvis.stock.domain.event.StockPriceUpdateEvent;
import com.truvis.stock.infrastructure.kis.KisApprovalResponse;
import com.truvis.stock.infrastructure.kis.KisRealtimePriceData;
import com.truvis.stock.infrastructure.kis.KisWebSocketResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KIS WebSocket 클라이언트
 * - 실시간 주식 체결가 수신
 * - 도메인 이벤트 발행
 */
@Slf4j
@Component
@Profile({"local", "dev", "test"})
public class KisWebSocketClient implements WebSocketHandler {
    
    private static final String WS_URL = "ws://ops.koreainvestment.com:21000";
    private static final String APPROVAL_URL = "https://openapi.koreainvestment.com:9443/oauth2/Approval";
    
    @Value("${kis.api.app-key:}")
    private String appKey;
    
    @Value("${kis.api.app-secret:}")
    private String appSecret;
    
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    private WebSocketSession session;
    private String approvalKey;
    private final Set<String> subscribedStocks = ConcurrentHashMap.newKeySet();
    
    public KisWebSocketClient(ApplicationEventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 초기화 - Approval Key 발급 및 WebSocket 연결
     */
    @PostConstruct
    public void initialize() {
        try {
            log.info("[KIS_WS] WebSocket 클라이언트 초기화 시작");
            
            // 1. Approval Key 발급
            issueApprovalKey();
            
            // 2. WebSocket 연결
            connect();
            
            log.info("[KIS_WS] WebSocket 클라이언트 초기화 완료");
        } catch (Exception e) {
            log.error("[KIS_WS] 초기화 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * WebSocket 연결 상태 확인
     */
    public boolean isConnected() {
        return session != null && session.isOpen();
    }
    
    /**
     * WebSocket 헬스 체크
     */
    public boolean isHealthy() {
        return isConnected() && approvalKey != null;
    }
    
    /**
     * 재연결
     */
    public void reconnect() {
        try {
            log.info("[KIS_WS] 재연결 시작...");
            
            // 기존 연결 종료
            if (session != null && session.isOpen()) {
                session.close();
            }
            
            // 새로 연결
            connect();
            
            // 기존 구독 복구
            log.info("[KIS_WS] 기존 구독 복구 중: {}개 종목", subscribedStocks.size());
            Set<String> stocksToResubscribe = new HashSet<>(subscribedStocks);
            subscribedStocks.clear();
            
            for (String stockCode : stocksToResubscribe) {
                subscribe(StockCode.of(stockCode));
                Thread.sleep(100);
            }
            
            log.info("[KIS_WS] 재연결 완료");
        } catch (Exception e) {
            log.error("[KIS_WS] 재연결 실패: {}", e.getMessage());
        }
    }
    
    /**
     * Approval Key 발급
     */
    private void issueApprovalKey() {
        log.info("[KIS_WS] Approval Key 발급 시작");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("secretkey", appSecret);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<KisApprovalResponse> response = restTemplate.postForEntity(
                    APPROVAL_URL,
                    request,
                    KisApprovalResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                this.approvalKey = response.getBody().getApprovalKey();
                log.info("[KIS_WS] Approval Key 발급 완료");
            } else {
                throw new RuntimeException("Approval Key 발급 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("[KIS_WS] Approval Key 발급 실패: {}", e.getMessage());
            throw new RuntimeException("Approval Key 발급 실패", e);
        }
    }
    
    /**
     * WebSocket 연결
     */
    private void connect() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            this.session = client.execute(this, WS_URL).get();
            log.info("[KIS_WS] WebSocket 연결 완료");
        } catch (Exception e) {
            log.error("[KIS_WS] WebSocket 연결 실패: {}", e.getMessage());
            throw new RuntimeException("WebSocket 연결 실패", e);
        }
    }
    
    /**
     * 종목 구독
     */
    public void subscribe(StockCode stockCode) {
        if (subscribedStocks.contains(stockCode.getValue())) {
            log.debug("[KIS_WS] 이미 구독 중인 종목: {}", stockCode.getValue());
            return;
        }
        
        try {
            Map<String, Object> request = createSubscribeRequest(stockCode.getValue(), "1");
            String json = objectMapper.writeValueAsString(request);
            
            session.sendMessage(new TextMessage(json));
            subscribedStocks.add(stockCode.getValue());
            
            log.info("[KIS_WS] 종목 구독 완료: {}", stockCode.getValue());
        } catch (Exception e) {
            log.error("[KIS_WS] 종목 구독 실패: {}", stockCode.getValue(), e);
        }
    }
    
    /**
     * 종목 구독 해제
     */
    public void unsubscribe(StockCode stockCode) {
        try {
            Map<String, Object> request = createSubscribeRequest(stockCode.getValue(), "2");
            String json = objectMapper.writeValueAsString(request);
            
            session.sendMessage(new TextMessage(json));
            subscribedStocks.remove(stockCode.getValue());
            
            log.info("[KIS_WS] 종목 구독 해제: {}", stockCode.getValue());
        } catch (Exception e) {
            log.error("[KIS_WS] 종목 구독 해제 실패: {}", stockCode.getValue(), e);
        }
    }
    
    /**
     * 구독 요청 메시지 생성
     */
    private Map<String, Object> createSubscribeRequest(String stockCode, String trType) {
        Map<String, Object> header = new HashMap<>();
        header.put("approval_key", approvalKey);
        header.put("custtype", "P");
        header.put("tr_type", trType);  // 1: 등록, 2: 해제
        header.put("content-type", "utf-8");
        
        Map<String, String> input = new HashMap<>();
        input.put("tr_id", "H0STCNT0");  // 실시간 체결가
        input.put("tr_key", stockCode);
        
        Map<String, Object> body = new HashMap<>();
        body.put("input", input);
        
        Map<String, Object> request = new HashMap<>();
        request.put("header", header);
        request.put("body", body);
        
        return request;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[KIS_WS] WebSocket 연결 수립: {}", session.getId());
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            handleTextMessage(payload);
        }
    }
    
    /**
     * 텍스트 메시지 처리
     */
    private void handleTextMessage(String payload) {
        try {
            log.debug("[KIS_WS] 수신: {}", payload);

            // 1. JSON 응답인지 확인 (구독 성공/실패 메시지 또는 PINGPONG)
            if (payload.startsWith("{")) {
                KisWebSocketResponse response = objectMapper.readValue(payload, KisWebSocketResponse.class);
                
                // PINGPONG 처리 (Heartbeat)
                if ("PINGPONG".equals(response.getHeader().getTrId())) {
                    log.debug("[KIS_WS] Heartbeat 수신");
                    return;
                }
                
                // body가 null인 경우 처리
                if (response.getBody() == null) {
                    log.debug("[KIS_WS] Body가 없는 메시지 수신: {}", response.getHeader().getTrId());
                    return;
                }
                
                // 구독 응답 메시지
                String rtCd = response.getBody().getRtCd();
                String msg = response.getBody().getMsg1();
                
                if ("0".equals(rtCd)) {
                    log.info("[KIS_WS] 구독 성공: {}", msg);
                } else {
                    log.error("[KIS_WS] 구독 실패 [{}]: {}", rtCd, msg);
                }
                return;
            }

            // 2. ^ 구분 실시간 데이터 파싱
            if (payload.contains("^")) {
                KisRealtimePriceData data = KisRealtimePriceData.parse(payload);
                publishPriceUpdateEvent(data);
            }
        } catch (Exception e) {
            log.error("[KIS_WS] 메시지 처리 실패: {}", e.getMessage());
        }
    }

    /**
     * 가격 변경 이벤트 발행
     * - 동기 방식으로 발행 (Async 충돌 방지)
     */
    private void publishPriceUpdateEvent(KisRealtimePriceData data) {
        try {
            StockCode stockCode = StockCode.of(data.getStockCode());
            
            // 체결시각 파싱 (HHMMSS -> LocalDateTime)
            LocalDateTime tradeTime = parseTradeTime(data.getTradeTime());
            
            // 도메인 이벤트 생성
            StockPriceUpdateEvent event = StockPriceUpdateEvent.of(
                    stockCode,
                    data.getCurrentPrice(),
                    data.getPriceChange(),
                    data.getChangeRate(),
                    tradeTime,
                    data.getTradeVolume()
            );
            
            // ✅ 동기 이벤트 발행 (Async 충돌 방지)
            eventPublisher.publishEvent(event);
            
            log.info("🔥 [KIS_WS] 실시간 체결가: {} = {}원 ({}%) [거래량: {}]", 
                    stockCode.getValue(), 
                    data.getCurrentPrice(), 
                    data.getChangeRate(),
                    data.getTradeVolume());
        } catch (Exception e) {
            log.error("[KIS_WS] 이벤트 발행 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 체결시각 파싱 (HHMMSS -> LocalDateTime)
     */
    private LocalDateTime parseTradeTime(String timeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
            LocalDateTime now = LocalDateTime.now();
            return now.withHour(Integer.parseInt(timeStr.substring(0, 2)))
                    .withMinute(Integer.parseInt(timeStr.substring(2, 4)))
                    .withSecond(Integer.parseInt(timeStr.substring(4, 6)))
                    .withNano(0);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[KIS_WS] 전송 오류: {}", exception.getMessage());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.warn("[KIS_WS] WebSocket 연결 종료: {}", closeStatus);
        
        // 재연결 시도
        reconnect();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 종료 시 WebSocket 연결 해제
     */
    @PreDestroy
    public void destroy() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
                log.info("[KIS_WS] WebSocket 연결 종료");
            }
        } catch (Exception e) {
            log.error("[KIS_WS] 연결 종료 실패: {}", e.getMessage());
        }
    }
}
