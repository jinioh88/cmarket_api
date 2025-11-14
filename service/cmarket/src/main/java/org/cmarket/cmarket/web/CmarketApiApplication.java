package org.cmarket.cmarket.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.cmarket")
public class CmarketApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CmarketApiApplication.class, args);
	}

}

