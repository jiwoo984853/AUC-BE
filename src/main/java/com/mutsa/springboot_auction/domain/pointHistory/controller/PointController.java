package com.mutsa.springboot_auction.domain.pointHistory.controller;

import com.mutsa.springboot_auction.domain.pointHistory.entity.PointHistory;
import com.mutsa.springboot_auction.domain.pointHistory.service.PointService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.global.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final UserRepository userRepository;

    @PostMapping("/charge")
    public void chargePoint(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            Map<String, Integer> request
    ) {
        User user = oAuth2User.getUser();
        Integer amount = request.get("amount");
        pointService.chargePoint(user, amount);
    }

    @GetMapping("/history")
    public List<PointHistory> getHistory(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        User user = oAuth2User.getUser();
        return pointService.getHistory(user);
    }

    /**
    // 포인트 충전 (테스트용)
    @PostMapping("/charge/test")
    public void chargePoint(@RequestBody Map<String, Integer> request) {
        Long userId = request.get("userId").longValue();
        Integer amount = request.get("amount");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        pointService.chargePoint(user, amount);
    }

    // 포인트 내역 조회 (테스트용)
    @GetMapping("/history/test")
    public List<PointHistory> getHistory(@RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return pointService.getHistory(user);
    }
    **/
}
