package com.truvis.common.filter;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 모든 HTTP 요청에 대해 MDC를 자동 설정하는 Filter
 * 
 * 역할:
 * 1. 요청마다 고유한 requestId 생성 및 MDC 저장
 * 2. userId는 JwtAuthenticationFilter에서 인증 성공 시 설정됨
 * 3. 요청 완료 후 MDC 정리
 * 
 * 실행 순서:
 * - @Order(Ordered.HIGHEST_PRECEDENCE)로 가장 먼저 실행되어 requestId 설정
 * - JwtAuthenticationFilter가 인증 성공 시 userId를 MDC에 설정
 * - 비동기 작업에서는 MdcTaskDecorator가 MDC를 전파
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // requestId는 가장 먼저 설정
@Slf4j
public class MdcLoggingFilter implements Filter {

    private static final String REQUEST_ID_KEY = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        try {
            // 1. 요청 고유 ID 생성 및 MDC 저장
            String requestId = generateRequestId();
            MDC.put(REQUEST_ID_KEY, requestId);
            
            // 2. 요청 시작 로그
            log.debug("🔵 요청 시작: requestId={}", requestId);
            
            // 3. 다음 필터 체인으로 전달 (JwtAuthenticationFilter에서 userId 설정됨)
            chain.doFilter(request, response);
            
            // 4. 요청 완료 로그 (userId는 이미 JwtAuthenticationFilter에서 설정됨)
            log.debug("🟢 요청 완료: requestId={}", requestId);
            
        } finally {
            // 5. 요청 종료 후 MDC 정리 (메모리 누수 방지)
            MDC.clear();
        }
    }

    /**
     * 요청 고유 ID 생성
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);  // 앞 8자리만 사용
    }
}
