package com.truvis.stock.config;

import com.truvis.common.config.MdcTaskDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 * - 이벤트 리스너의 @Async 처리 활성화
 */
@Configuration
@EnableAsync
@Slf4j
public class StockAsyncConfig {

    @Bean(name = "stockExecutor")
    public Executor stockExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 스레드 풀 설정 (실시간 주가 이벤트 처리용)
        executor.setCorePoolSize(5);       // 기본 스레드 5개
        executor.setMaxPoolSize(20);       // 최대 스레드 20개
        executor.setQueueCapacity(200);    // 대기 큐 200개
        executor.setThreadNamePrefix("stock-async-");

        // 🎯 MDC와 SecurityContext 전달 (비동기 작업에서도 로그 추적 가능)
        executor.setTaskDecorator(new MdcTaskDecorator());

        // 거부 정책
        executor.setRejectedExecutionHandler((r, e) -> {
            log.error("❌ 주가 이벤트 큐가 가득 찼습니다!");
        });

        // 종료 시 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("📈 Stock 비동기 Executor 설정 완료: core={}, max={}, queue={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity());

        return executor;
    }
}
