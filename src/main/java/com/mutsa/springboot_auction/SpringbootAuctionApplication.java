package com.mutsa.springboot_auction;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
@Slf4j
public class SpringbootAuctionApplication {

	public static void main(String[] args) {
		log.info("==========================================================");
		SpringApplication.run(SpringbootAuctionApplication.class, args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}
}
