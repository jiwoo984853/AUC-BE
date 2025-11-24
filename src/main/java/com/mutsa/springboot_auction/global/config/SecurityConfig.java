package com.mutsa.springboot_auction.global.config;

import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import com.mutsa.springboot_auction.domain.user.service.UserService;
import com.mutsa.springboot_auction.global.config.jwt.TokenProvider;
import com.mutsa.springboot_auction.global.config.jwt.repository.RefreshTokenRepository;
import com.mutsa.springboot_auction.global.config.oauth.filter.TokenAuthenticationFilter;
import com.mutsa.springboot_auction.global.config.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.mutsa.springboot_auction.global.config.oauth.service.OAuth2SuccessHandler;
import com.mutsa.springboot_auction.global.config.oauth.service.OAuth2UserCustomService;
import java.util.Arrays;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity  // Spring Security 활성화
@RequiredArgsConstructor
public class SecurityConfig {


    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    /**
     * Spring Security 필터 체인 설정
     * 모든 보안 규칙을 정의하는 핵심 메서드
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 보안 필터 체인
     * @throws Exception 설정 오류 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // === CSRF 보안 설정 ===
                // REST API에서는 CSRF 공격 위험이 적으므로 비활성화
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frameOptionsConfig -> frameOptionsConfig.disable()) // H2 콘솔 iframe 허용
                )

                // === CORS 설정 ===
                // 프론트엔드에서 API 호출 시 필요
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // === 세션 관리 설정 ===
                // JWT 토큰을 사용하므로 세션을 생성하지 않음
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // === 요청 권한 설정 ===
                .authorizeHttpRequests(authz -> authz
                        // 인증 없이 접근 가능한 경로들
                        .requestMatchers(
                                "/",
                                "/login/**",
                                "/token/**",
                                "/oauth2/**",
                                "/h2-console/**",
                                "/login/oauth2/**",
                                "/api/auth/**",
                                "/api/s3/**",          // ← 패턴 변경
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/ws/**",              // WebSocket 엔드포인트
                                "/app/**",             // STOMP 메시지 발행
                                "/topic/**",           // STOMP 구독
                                "/queue/**",           // ← 추가 (개인 메시지용)
                                "/chat-test.html",
                                "/api/search/**",
                                "/hc",
                                "/error",
                                "/auctions/test"
                        ).permitAll()

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // === OAuth2 로그인 설정 ===
                .oauth2Login(oauth2 -> oauth2
                        // 로그인 페이지 URL
                        .loginPage("/login")

                        // ★ AuthorizationRequestRepository 설정
                        .authorizationEndpoint(authorization ->
                                authorization.authorizationRequestRepository(
                                        oAuth2AuthorizationRequestBasedOnCookieRepository()
                                )
                        )

                        // 카카오 사용자 정보 처리 서비스
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(oAuth2UserCustomService)
                        )

                        // OAuth2 로그인 성공 시 처리 핸들러
                        .successHandler(oAuth2SuccessHandler())
                )

                // === 예외 처리 ===
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestURI = request.getRequestURI();

                            // /api/** 는 401 반환
                            if (requestURI.startsWith("/api/")) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            } else {
                                // 그 외는 로그인 페이지로
                                response.sendRedirect("/login");
                            }
                        })
                )

                // === 커스텀 필터 추가 ===
                // JWT 인증 필터를 Spring Security 필터 체인에 추가
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     * 프론트엔드(React, Vue 등)에서 백엔드 API 호출 시 필요
     *
     * @return CorsConfigurationSource CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ 배포 도메인 추가
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://plip-aution.vercel.app",
                "https://www.plip.store",
                "http://localhost:8080",
                "https://mmuuttssaa.shop",      // ✅ 추가
                "http://mmuuttssaa.shop"        // ✅ 추가
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                userRepository
        );
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

