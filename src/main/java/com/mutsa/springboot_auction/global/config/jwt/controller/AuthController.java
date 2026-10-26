package com.mutsa.springboot_auction.global.config.jwt.controller;

import com.mutsa.springboot_auction.global.config.jwt.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${auth.refresh-cookie-secure:false}")
    private boolean secureRefreshCookie;

    @PostMapping("/api/auth/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.findByRefreshToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(secureRefreshCookie)
                .sameSite(secureRefreshCookie ? "None" : "Lax")
                .path("/")
                .maxAge(0)
                .build().toString());
        return "로그아웃 성공";
    }
}
