package com.truvis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.truvis"})
public class truvisApplication {
	public static void main(String[] args) {
		SpringApplication.run(truvisApplication.class, args);
	}

}
