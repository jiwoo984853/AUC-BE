package com.mutsa.springboot_auction.global.config.jwt.controller;

import com.mutsa.springboot_auction.domain.user.KakaoUserInfo;
import com.mutsa.springboot_auction.domain.user.entity.Role;
import com.mutsa.springboot_auction.domain.user.entity.SocialType;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import com.mutsa.springboot_auction.global.config.jwt.TokenProvider;
import com.mutsa.springboot_auction.global.config.jwt.domain.RefreshToken;
import com.mutsa.springboot_auction.global.config.jwt.dto.AppTokenRequest;
import com.mutsa.springboot_auction.global.config.jwt.dto.AppTokenResponse;
import com.mutsa.springboot_auction.global.config.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AppAuthController {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/kakao")
    public ResponseEntity<AppTokenResponse> kakaoLogin(@RequestBody AppTokenRequest request) {
        String kakaoUserInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                kakaoUserInfoUrl,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> attributes = response.getBody();
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(attributes);

        String socialId = kakaoUserInfo.getId();

        Optional<User> userOptional = userRepository.findBySocialId(socialId);

        User user = userOptional.orElseGet(() -> userRepository.save(User.builder()
                .socialId(socialId)
                .nickname(kakaoUserInfo.getNickname()) // 초기 임시 닉네임 설정
                .socialType(SocialType.KAKAO)
                .role(Role.USER)
                .build()));

        Boolean isFirstLogin = (user.getProfileImageUrl() == null);

        String accessToken = tokenProvider.generateToken(user, Duration.ofDays(1));
        String refreshToken = tokenProvider.generateToken(user, Duration.ofDays(14));

        saveRefreshToken(user.getId(), refreshToken);

        return ResponseEntity.ok(new AppTokenResponse(accessToken, refreshToken, isFirstLogin));
    }

    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(e -> e.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));
        refreshTokenRepository.save(refreshToken);
    }
}