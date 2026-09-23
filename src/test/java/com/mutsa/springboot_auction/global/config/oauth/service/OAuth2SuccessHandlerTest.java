package com.mutsa.springboot_auction.global.config.oauth.service;

import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import com.mutsa.springboot_auction.global.config.jwt.TokenProvider;
import com.mutsa.springboot_auction.global.config.jwt.repository.RefreshTokenRepository;
import com.mutsa.springboot_auction.global.config.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OAuth2SuccessHandlerTest {
    @Test
    void productionLoginSetsProtectedCookieWithoutTokensInRedirect() throws Exception {
        TokenProvider tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.generateToken(any(User.class), any(Duration.class))).thenReturn("secret-refresh");
        RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);
        UserRepository users = mock(UserRepository.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        when(users.findById(42L)).thenReturn(Optional.of(user));
        OAuth2AuthorizationRequestBasedOnCookieRepository requests =
                mock(OAuth2AuthorizationRequestBasedOnCookieRepository.class);
        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(tokenProvider, refreshTokens, requests, users);
        ReflectionTestUtils.setField(handler, "secureRefreshCookie", true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "https://www.plip.store");
        MockHttpServletResponse response = new MockHttpServletResponse();
        var authentication = new UsernamePasswordAuthenticationToken(
                new CustomOAuth2User(user, Map.of()), null);

        handler.onAuthenticationSuccess(request, response, authentication);

        String cookie = response.getHeader("Set-Cookie");
        assertNotNull(cookie);
        assertTrue(cookie.contains("refresh_token=secret-refresh"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("SameSite=None"));
        assertTrue(response.getRedirectedUrl().startsWith("https://www.plip.store/auth/kakao/callback"));
        assertFalse(response.getRedirectedUrl().contains("secret-refresh"));
        assertFalse(response.getRedirectedUrl().contains("accessToken"));
    }
}
