package com.mutsa.springboot_auction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@Slf4j
public class SpringbootAuctionApplication {

	public static void main(String[] args) {
		log.info("==========================================================");
		SpringApplication.run(SpringbootAuctionApplication.class, args);
	}
}
