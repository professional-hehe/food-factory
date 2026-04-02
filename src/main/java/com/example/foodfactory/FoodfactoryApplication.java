package com.example.foodfactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync   // enables @Async on EmailService
public class FoodfactoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodfactoryApplication.class, args);
	}

}
