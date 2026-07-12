package com.nagaralert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NagarAlertHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(NagarAlertHubApplication.class, args);
	}

}