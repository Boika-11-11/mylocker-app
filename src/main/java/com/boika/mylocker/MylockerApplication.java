package com.boika.mylocker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MylockerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MylockerApplication.class, args);
	}

}