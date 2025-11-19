package com.mutsa.springboot_auction.global.config.jwt.controller;

import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.global.config.jwt.domain.RefreshToken;
import com.mutsa.springboot_auction.global.config.jwt.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private RefreshTokenRepository refreshTokenRepository;

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal
                         CustomOAuth2User customOAuth2User) {
        Long userId = customOAuth2User.getUser().getId();
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        if (refreshTokenRepository.findByUserId(userId).isPresent()) {
            RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId).get();
            refreshTokenRepository.delete(refreshToken);
        }
        return "로그아웃 성공";
    }
}
