package com.example.PaySathi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PaySathiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaySathiApplication.class, args);
	}

}
