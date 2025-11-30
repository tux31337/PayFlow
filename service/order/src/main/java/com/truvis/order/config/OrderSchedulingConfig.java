package com.truvis.order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Order 모듈 스케줄링 설정
 * 
 * @EnableScheduling: Spring의 @Scheduled 어노테이션 활성화
 * - OrderMatchingEngine의 주기적 실행을 가능하게 함
 */
@Configuration
@EnableScheduling
public class OrderSchedulingConfig {
    // OrderMatchingEngine의 @Scheduled 메서드가 1초마다 실행됨
}
