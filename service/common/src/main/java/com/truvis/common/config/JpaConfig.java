package com.truvis.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정
 * 
 * - @EnableJpaAuditing: BaseEntity의 @CreatedDate, @LastModifiedDate 자동 주입 활성화
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}

