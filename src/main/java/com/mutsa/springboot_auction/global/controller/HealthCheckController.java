package com.mutsa.springboot_auction.global.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    String redirectUri;

    @GetMapping("/hc")
    public String hc() {
        return redirectUri;
    }
}
