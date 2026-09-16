package com.AutoFacApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoFacApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoFacApiApplication.class, args);
	}

}
