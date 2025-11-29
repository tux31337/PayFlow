package com.truvis.stock.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * TimescaleDB DataSource Configuration (Secondary)
 * 
 * Infrastructure Layer:
 * - 시계열 데이터 전용 DB 설정 (StockPriceHistory)
 * - Primary(H2)와 완전히 분리된 EntityManagerFactory
 * - timescale 서브패키지만 스캔
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.truvis.stock.repository.timescale",
        entityManagerFactoryRef = "timescaleEntityManagerFactory",
        transactionManagerRef = "timescaleTransactionManager"
)
public class TimescaleDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource-timescale")
    public DataSourceProperties timescaleDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "timescaleDataSource")
    @Lazy
    public DataSource timescaleDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(timescaleDataSourceProperties().getUrl());
        config.setUsername(timescaleDataSourceProperties().getUsername());
        config.setPassword(timescaleDataSourceProperties().getPassword());
        config.setDriverClassName(timescaleDataSourceProperties().getDriverClassName());
        
        // 연결 실패 시에도 애플리케이션이 시작되도록 설정
        config.setInitializationFailTimeout(-1);
        config.setConnectionTimeout(30000);
        config.setMaximumPoolSize(5);
        
        return new HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean timescaleEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "true");
        properties.put("hibernate.jdbc.batch_size", "50");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        
        return builder
                .dataSource(timescaleDataSource())
                .packages("com.truvis.stock.domain.timescale")  // timescale 서브패키지만 스캔
                .persistenceUnit("timescale")
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager timescaleTransactionManager(
            EntityManagerFactory timescaleEntityManagerFactory) {
        return new JpaTransactionManager(timescaleEntityManagerFactory);
    }
}
