package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.app")
public class JRPApplication {

	public static void main(String[] args) {
		SpringApplication.run(JRPApplication.class, args);
	}

}
