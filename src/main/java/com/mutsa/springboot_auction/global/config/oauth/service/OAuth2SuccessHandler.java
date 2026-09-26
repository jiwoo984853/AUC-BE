package com.mutsa.springboot_auction.global.config.oauth.service;

import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import com.mutsa.springboot_auction.global.config.jwt.TokenProvider;
import com.mutsa.springboot_auction.global.config.jwt.domain.RefreshToken;
import com.mutsa.springboot_auction.global.config.jwt.repository.RefreshTokenRepository;
import com.mutsa.springboot_auction.global.config.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final String DEFAULT_REDIRECT_PATH = "https://auc-fe.vercel.app";
    public static final String CALLBACK_PATH = "/auth/kakao/callback";

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final UserRepository userRepository;
    @Value("${auth.refresh-cookie-secure:false}")
    private boolean secureRefreshCookie;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user.getId(), refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);

        String targetUrl = getTargetUrl(request, user.getId());

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

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(secureRefreshCookie)
                .sameSite(secureRefreshCookie ? "None" : "Lax")
                .path("/")
                .maxAge(cookieMaxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private String getTargetUrl(HttpServletRequest request, Long userId) {
        User user = userRepository.findById(userId).get();
        Boolean isFirstLogin = user.getProfileImageUrl() == null;
        
        String redirectBaseUrl = getRedirectBaseUrl(request);
        String redirectPath = redirectBaseUrl + CALLBACK_PATH;
        log.info("======================");
        log.info("redirect baseUrl = {}", redirectBaseUrl);
        log.info("======================");

        return UriComponentsBuilder.fromUriString(redirectPath)
                .queryParam("isFirstLogin", isFirstLogin)
                .build()
                .toUriString();
    }

    private static final Set<String> ALLOWED_REDIRECT_ORIGINS = Set.of(
            "https://auc-fe.vercel.app",
            "http://localhost:3000",
            "http://localhost:5173"
    );

    private String getRedirectBaseUrl(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        log.info("origin 오류? = {}", origin);
        if (origin != null && ALLOWED_REDIRECT_ORIGINS.contains(origin)) {
            log.info("origin 오류? = {}", origin);
            return origin;
        }

        String referer = request.getHeader("Referer");
        log.info("referer 오류? ={}", referer);
        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                String ref = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
                if (ALLOWED_REDIRECT_ORIGINS.contains(ref)) {
                    return ref;
                }
            } catch (Exception ignored) {}
        }

        return DEFAULT_REDIRECT_PATH; // https://auc-fe.vercel.app
    }
}
