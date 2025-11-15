package com.truvis.controller.stock;


import com.truvis.common.model.vo.StockCode;
import com.truvis.stock.application.StockApplicationService;
import com.truvis.stock.domain.Market;
import com.truvis.stock.infrastructure.sse.SseEmitterManager;
import com.truvis.stock.infrastructure.websocket.KisWebSocketClient;
import com.truvis.stock.model.StockDetailResponse;
import com.truvis.stock.model.StockResponse;
import com.truvis.stock.model.StockSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 종목 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockApplicationService stockApplicationService;
    private final SseEmitterManager sseEmitterManager;
    private final KisWebSocketClient kisWebSocketClient;

    /**
     * 종목 검색 (자동완성)
     * GET /api/stocks/search?keyword=삼성
     */
    @GetMapping("/search")
    public ResponseEntity<List<StockSearchResponse>> searchStocks(
            @RequestParam String keyword
    ) {
        List<StockSearchResponse> results = stockApplicationService.searchStocks(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 종목 상세 조회
     * GET /api/stocks/{stockCode}
     */
    @GetMapping("/{stockCode}")
    public ResponseEntity<StockDetailResponse> getStockDetail(
            @PathVariable String stockCode
    ) {
        StockDetailResponse response = stockApplicationService.getStockDetail(stockCode);
        return ResponseEntity.ok(response);
    }

    /**
     * 시장별 종목 조회
     * GET /api/stocks/market/{market}
     */
    @GetMapping("/market/{market}")
    public ResponseEntity<List<StockResponse>> getStocksByMarket(
            @PathVariable Market market
    ) {
        List<StockResponse> responses = stockApplicationService.getStocksByMarket(market);
        return ResponseEntity.ok(responses);
    }

    /**
     * 여러 종목 조회
     * POST /api/stocks/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<List<StockResponse>> getStocks(
            @RequestBody List<String> stockCodes
    ) {
        List<StockResponse> responses = stockApplicationService.getStocks(stockCodes);
        return ResponseEntity.ok(responses);
    }

    /**
     * 종목 등록 (관리자)
     * POST /api/stocks
     */
    @PostMapping
    public ResponseEntity<StockResponse> registerStock(
            @RequestBody RegisterStockRequest request
    ) {
        StockResponse response = stockApplicationService.registerStock(
                request.getStockCode(),
                request.getName(),
                request.getMarket(),
                request.getSector()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 실시간 주가 스트림 (SSE)
     * GET /api/stocks/{stockCode}/price/stream
     * 
     * 예: curl -N http://localhost:8080/api/stocks/005930/price/stream
     */
    @GetMapping(value = "/{stockCode}/price/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStockPrice(@PathVariable String stockCode) {
        log.info("[SSE] 실시간 가격 구독 요청: {}", stockCode);
        
        // 1. SSE Emitter 생성
        SseEmitter emitter = sseEmitterManager.createEmitter(stockCode);
        
        // 2. KIS WebSocket 구독 (아직 구독하지 않은 경우)
        try {
            kisWebSocketClient.subscribe(StockCode.of(stockCode));
        } catch (Exception e) {
            log.error("[SSE] WebSocket 구독 실패: {}", stockCode, e);
        }
        
        return emitter;
    }

    // Request DTO
    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RegisterStockRequest {
        private String stockCode;
        private String name;
        private Market market;
        private String sector;
    }
}