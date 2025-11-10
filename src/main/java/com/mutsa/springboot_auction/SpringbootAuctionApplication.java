package com.mutsa.springboot_auction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SpringbootAuctionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootAuctionApplication.class, args);
	}



}
