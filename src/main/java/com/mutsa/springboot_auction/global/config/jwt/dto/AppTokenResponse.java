package com.mutsa.springboot_auction.global.config.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppTokenResponse {
    private String accessToken;
    private String refreshToken;
    private Boolean isFirstLogin;
}

