package com.mutsa.springboot_auction.global.config.jwt.controller;

import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.global.config.jwt.domain.RefreshToken;
import com.mutsa.springboot_auction.global.config.jwt.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${auth.refresh-cookie-secure:false}")
    private boolean secureRefreshCookie;

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal
                         CustomOAuth2User customOAuth2User) {
        Long userId = customOAuth2User.getUser().getId();
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        if (refreshTokenRepository.findByUserId(userId).isPresent()) {
            RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId).get();
            refreshTokenRepository.delete(refreshToken);
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
