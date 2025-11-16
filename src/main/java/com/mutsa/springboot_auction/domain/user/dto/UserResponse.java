package com.mutsa.springboot_auction.domain.user.dto;

import com.mutsa.springboot_auction.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserResponse {
    private Long user_id;
    private String nickname;
    private String profileImageUrl;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getNickname(), user.getProfileImageUrl());
    }
}
