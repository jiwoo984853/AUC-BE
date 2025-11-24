package com.mutsa.springboot_auction.global.config.oauth.service;

import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import com.mutsa.springboot_auction.global.config.jwt.TokenProvider;
import com.mutsa.springboot_auction.global.config.jwt.domain.RefreshToken;
import com.mutsa.springboot_auction.global.config.jwt.repository.RefreshTokenRepository;
import com.mutsa.springboot_auction.global.config.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.mutsa.springboot_auction.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    public static final String DEFAULT_REDIRECT_PATH = "https://www.plip.store";
    public static final String CALLBACK_PATH = "/auth/kakao/callback";

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final UserRepository userRepository;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user.getId(), refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);

        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);

        log.info("token : {}", accessToken);
        String targetUrl = getTargetUrl(request, accessToken, user.getId());

        clearAuthenticationAttributes(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));

        refreshTokenRepository.save(refreshToken);
    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();

        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private String getTargetUrl(HttpServletRequest request, String token, Long userId) {
        User user = userRepository.findById(userId).get();
        Boolean isFirstLogin = user.getProfileImageUrl() == null;
        
        String redirectBaseUrl = getRedirectBaseUrl(request);
        String redirectPath = redirectBaseUrl + CALLBACK_PATH;
        
        return UriComponentsBuilder.fromUriString(redirectPath)
                .queryParam("token", token)
                .queryParam("isFirstLogin", isFirstLogin)
                .build()
                .toUriString();
    }
    
    private String getRedirectBaseUrl(HttpServletRequest request) {
//        String origin = request.getHeader("Origin");
//        if (origin != null && !origin.isEmpty()) {
//            return origin;
//        }
//
//        String referer = request.getHeader("Referer");
//        if (referer != null && !referer.isEmpty()) {
//            try {
//                URI uri = URI.create(referer);
//                return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
//            } catch (Exception e) { }
//        }
        
        return DEFAULT_REDIRECT_PATH;
    }
}
