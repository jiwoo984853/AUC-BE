package com.mutsa.springboot_auction.global.config.oauth.service;

import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.Role;
import com.mutsa.springboot_auction.domain.user.entity.SocialType;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String socialId = oAuth2User.getName();

        User user = userRepository.findBySocialId(socialId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .socialId(socialId)
                        .nickname(extractNickname(attributes))
                        .socialType(SocialType.KAKAO)
                        .role(Role.USER)
                        .build()));

        return new CustomOAuth2User(user, attributes);
    }

    private String extractNickname(Map<String, Object> attributes) {
        if (attributes.get("kakao_account") instanceof Map<?, ?> account
                && account.get("profile") instanceof Map<?, ?> profile
                && profile.get("nickname") instanceof String nickname
                && !nickname.isBlank()) {
            return nickname;
        }

        throw new OAuth2AuthenticationException(
                new OAuth2Error("missing_kakao_nickname"),
                "카카오 닉네임 제공 동의를 확인해주세요.");
    }
}
