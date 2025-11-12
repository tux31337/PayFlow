package com.truvis.stock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 비동기 처리 설정
 * - 이벤트 리스너의 @Async 처리 활성화
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
