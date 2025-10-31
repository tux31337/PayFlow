package com.truvis.common.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * 비동기 작업 시 MDC와 SecurityContext를 전달하는 TaskDecorator
 * 
 * MDC(Mapped Diagnostic Context)는 ThreadLocal 기반이므로
 * @Async로 실행되는 새로운 스레드에는 자동으로 전달되지 않습니다.
 * 
 * 이 Decorator를 사용하면:
 * - 로그 추적을 위한 requestId, userId 등이 비동기 작업에서도 유지됨
 * - Spring Security의 인증 정보도 비동기 작업에서 접근 가능
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 현재 스레드의 MDC 컨텍스트 복사
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        // 현재 스레드의 SecurityContext 복사
        SecurityContext securityContext = SecurityContextHolder.getContext();
        
        return () -> {
            try {
                // 새 스레드에 MDC 설정
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                
                // 새 스레드에 SecurityContext 설정
                SecurityContextHolder.setContext(securityContext);
                
                // 실제 작업 실행
                runnable.run();
                
            } finally {
                // 작업 완료 후 정리 (메모리 누수 방지)
                MDC.clear();
                SecurityContextHolder.clearContext();
            }
        };
    }
}
