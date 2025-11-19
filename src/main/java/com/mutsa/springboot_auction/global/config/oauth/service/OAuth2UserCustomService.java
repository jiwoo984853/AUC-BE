package com.mutsa.springboot_auction.global.config.oauth.service;

import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.Role;
import com.mutsa.springboot_auction.domain.user.entity.SocialType;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest); // ❶ 요청을 바탕으로 유저 정보를 담은 객체 반환
        Map<String, Object> attributes = oAuth2User.getAttributes();
        User user = saveOrUpdate(oAuth2User);

        return new CustomOAuth2User(user,attributes);
    }

    // ❷ 유저가 있으면 업데이트, 없으면 유저 생성
    private User saveOrUpdate(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.info("=================================");
        log.info("{}", attributes);
        log.info("=================================");

//        String socialId = (String) attributes.get("id");
        String socialId = (String) oAuth2User.getName();

        String name = (String) attributes.get("name");

        User user = userRepository.findBySocialId(socialId)
                .orElse(User.builder()
                        .socialId(socialId)
                        .nickname(name)
                        .socialType(SocialType.KAKAO)
                        .role(Role.USER)
                        .build());

        return userRepository.save(user);
    }
}

