package com.mutsa.springboot_auction.global.config.jwt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AppTokenRequest {
    private String accessToken;
    private String refreshToken;
}
