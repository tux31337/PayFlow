package com.truvis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.truvis"})
@EnableScheduling  // 스케줄링 활성화
public class TruvisApplication {
	public static void main(String[] args) {
		SpringApplication.run(TruvisApplication.class, args);
	}

}
