package com.truvis.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

/**
 * Primary DataSource Configuration (H2)
 * - 도메인 엔티티용 (User, Stock, Transaction 등)
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.truvis.user.repository",
                "com.truvis.stock.repository",
                "com.truvis.stock.infrastructure",
                "com.truvis.transaction.repository",
                "com.truvis.transaction.domain",
                "com.truvis.portfolio.repository",
                "com.truvis.question.repository",
                "com.truvis.order.repository",
                "com.truvis.order.domain"
        },
        excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "com\\.truvis\\.stock\\.repository\\.timescale\\..*"
                )
        },
        entityManagerFactoryRef = "primaryEntityManagerFactory",
        transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        
        // Hibernate 속성 설정
        java.util.Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");  // 스키마 관리 안 함 - Flyway가 처리
        
        return builder
                .dataSource(primaryDataSource())
                .packages(
                        "com.truvis.user.domain",
                        "com.truvis.stock.domain",
                        "com.truvis.transaction.domain",
                        "com.truvis.portfolio.domain",
                        "com.truvis.question.domain",
                        "com.truvis.order.domain"
                )
                .persistenceUnit("primary")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager primaryTransactionManager(
            EntityManagerFactory primaryEntityManagerFactory) {
        return new JpaTransactionManager(primaryEntityManagerFactory);
    }
}

