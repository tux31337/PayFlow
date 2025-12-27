package com.truvis.notification.config;

import com.truvis.common.config.MdcTaskDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;  // 🎯 추가!
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 및 스케줄링 설정
 */
@Configuration
@EnableAsync
@EnableScheduling  // 🎯 추가!
@Slf4j
public class NotificationAsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "notificationExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 스레드 풀 설정
        executor.setCorePoolSize(10);     // 10개 (많이!)
        executor.setMaxPoolSize(50);      // 최대 50개
        executor.setQueueCapacity(500);   // 큐 500개
        executor.setThreadNamePrefix("notification-async-");

        // 🎯 MDC와 SecurityContext 전달 (비동기 작업에서도 로그 추적 가능)
        executor.setTaskDecorator(new MdcTaskDecorator());

        // 🎯 거부 정책
        executor.setRejectedExecutionHandler((r, e) -> {
            log.error("알림 큐가 가득 참");
        });

        // 🎯 종료 시 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("비동기 Executor 초기화: core={}, max={}, queue={}",
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
            log.error("🚨 비동기 작업 중 예외 발생!");
            log.error("메서드: {}", method.getName());
            log.error("파라미터: {}", params);
            log.error("예외: ", ex);
        };
    }
}