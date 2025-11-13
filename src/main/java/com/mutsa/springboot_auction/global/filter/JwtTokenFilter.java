package com.mutsa.springboot_auction.global.filter;

import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import com.mutsa.springboot_auction.global.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // permitAll 경로는 필터 건너뛰기
        return path.startsWith("/api/points/") ||
                path.startsWith("/api/s3/") ||
                path.startsWith("/ws/") ||
                path.startsWith("/api/auth/") ||
                path.equals("/") ||
                path.startsWith("/login") ||
                path.startsWith("/oauth2");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if(token != null && jwtUtil.validateToken(token)) {
            String socialId = jwtUtil.getSocialIdFromToken(token);

            if(socialId != null) {
                Optional<User> user = userRepository.findBySocialId(socialId);
                if(user.isPresent()) {
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            user.get(),
                            null,
                            user.get().getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Authorization 헤더가 존재하고 "Bearer "로 시작하는지 확인
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 부분을 제거하고 토큰만 반환
            return bearerToken.substring(7);
        }

        return null;
    }
}
