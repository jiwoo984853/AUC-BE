package com.mutsa.springboot_auction.global.config.jwt.controller;

import com.mutsa.springboot_auction.global.config.jwt.dto.CreateAccessTokenResponse;
import com.mutsa.springboot_auction.global.config.jwt.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TokenApiController {

    private final TokenService tokenService;

    @PostMapping("/token/access")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token cookie is required");
        }
        String newAccessToken;
        try {
            newAccessToken = tokenService.createNewAccessToken(refreshToken);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }
}
