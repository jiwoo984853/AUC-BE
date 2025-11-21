package com.mutsa.springboot_auction.domain.user.controller;

import com.mutsa.springboot_auction.domain.user.dto.ProfileRequest;
import com.mutsa.springboot_auction.domain.user.dto.UserResponse;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import com.mutsa.springboot_auction.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/user/profile")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody ProfileRequest request,
                                                      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        User user = customOAuth2User.getUser();
        return ResponseEntity.ok(UserResponse.from(userService.updateProfile(request, user)));
    }

    @GetMapping("/user/prifile")
    public ResponseEntity<UserResponse> getPofile(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        User user = customOAuth2User.getUser();
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
