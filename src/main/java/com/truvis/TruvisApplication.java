package com.truvis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.truvis"})
public class TruvisApplication {
	public static void main(String[] args) {
		SpringApplication.run(TruvisApplication.class, args);
	}

}
