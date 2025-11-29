package com.truvis;

import com.truvis.stock.config.TimescaleDataSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.truvis"})
@EnableScheduling  // 스케줄링 활성화
@Import(TimescaleDataSourceConfig.class)  // TimescaleDB 설정 Import
public class TruvisApplication {
	public static void main(String[] args) {
		SpringApplication.run(TruvisApplication.class, args);
	}

}
