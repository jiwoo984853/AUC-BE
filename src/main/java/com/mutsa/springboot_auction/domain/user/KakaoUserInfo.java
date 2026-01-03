package com.mutsa.springboot_auction.domain.user;

import java.util.Map;
import lombok.Getter;

@Getter
public class KakaoUserInfo {
    private String id;
    private String nickname;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.id = String.valueOf(attributes.get("id"));

        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties != null) {
            this.nickname = (String) properties.get("nickname");
        }

        if (this.nickname == null) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    this.nickname = (String) profile.get("nickname");
                }
            }
        }
    }
}