package com.truvis.transaction.config;

import com.truvis.common.config.MdcTaskDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class TransactionAsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "transactionExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 스레드 풀 설정 (거래는 알림보다 적음)
        executor.setCorePoolSize(3);       // 기본 스레드 3개
        executor.setMaxPoolSize(10);       // 최대 스레드 10개
        executor.setQueueCapacity(100);    // 대기 큐 100개
        executor.setThreadNamePrefix("transaction-async-");

        // 🎯 MDC와 SecurityContext 전달 (비동기 작업에서도 로그 추적 가능)
        executor.setTaskDecorator(new MdcTaskDecorator());

        // 거부 정책
        executor.setRejectedExecutionHandler((r, e) -> {
            log.error("❌ 거래 이벤트 큐가 가득 찼습니다!");
        });

        // 종료 시 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("🚀 Transaction 비동기 Executor 설정 완료: core={}, max={}, queue={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity());

        return executor;
    }

    /**
     * 비동기 작업 예외 핸들러
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("🚨 Transaction 비동기 작업 중 예외 발생!");
            log.error("  메서드: {}", method.getName());
            log.error("  파라미터: {}", params);
            log.error("  예외: ", ex);
        };
    }
}
