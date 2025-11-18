package com.mutsa.springboot_auction.domain.pointHistory.controller;

import com.mutsa.springboot_auction.domain.pointHistory.dto.TossPaymentConfirmRequest;
import com.mutsa.springboot_auction.domain.pointHistory.dto.TossPaymentResponse;
import com.mutsa.springboot_auction.domain.pointHistory.entity.PointHistory;
import com.mutsa.springboot_auction.domain.pointHistory.service.PointService;
import com.mutsa.springboot_auction.domain.pointHistory.service.TossPaymentService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final TossPaymentService tossPaymentService;

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


    // 포인트 충전 (테스트용)
    @PostMapping("/charge/test")
    public void chargePoint(@RequestBody Map<String, Integer> request) {
        Long userId = request.get("userId").longValue();
        Integer amount = request.get("amount");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        pointService.chargePoint(user, amount);
    }

    @GetMapping("/history/test")
    public List<PointHistory> getHistory(@RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return pointService.getHistory(user);
    }

    /**
     * 토스페이먼츠 결제 승인 API
     * 
     * @param oAuth2User JWT 토큰에서 추출한 사용자 정보 
     * @param request 결제 승인 요청 (paymentKey, orderId, amount)
     * @return 결제 승인 결과
     */
    @PostMapping("/charge/toss")
    public ResponseEntity<?> chargePointWithToss(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @RequestBody TossPaymentConfirmRequest request
    ) {
        try {
            // 1. JWT 토큰에서 사용자 정보 추출
            User user = oAuth2User.getUser();
            
            // 2. 토스페이먼츠 결제 승인 API 호출
            TossPaymentResponse paymentResponse = tossPaymentService.confirmPayment(request);
            
            // 3. 결제 상태 확인
            if (!"DONE".equals(paymentResponse.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "결제가 완료되지 않았습니다. 상태: " + paymentResponse.getStatus()));
            }
            
            // 4. 결제 금액 검증
            if (!request.getAmount().equals(paymentResponse.getTotalAmount())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "결제 금액이 일치하지 않습니다."));
            }
            
            // 5. 포인트 충전
            pointService.chargePoint(user, paymentResponse.getTotalAmount());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "포인트 충전이 완료되었습니다.",
                    "paymentKey", paymentResponse.getPaymentKey(),
                    "orderId", paymentResponse.getOrderId(),
                    "amount", paymentResponse.getTotalAmount(),
                    "userId", user.getId(),
                    "userPoint", user.getPoint()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 토스페이먼츠 결제 승인 API (테스트용 - userId 포함)
     * 
     * @param request 결제 승인 요청 및 userId 포함
     * @return 결제 승인 결과
     */
    @PostMapping("/charge/toss/test")
    public ResponseEntity<?> chargePointWithTossTest(@RequestBody Map<String, Object> request) {
        try {
            // 요청 데이터 파싱
            String paymentKey = (String) request.get("paymentKey");
            String orderId = (String) request.get("orderId");
            Integer amount = ((Number) request.get("amount")).intValue();
            Long userId = ((Number) request.get("userId")).longValue();
            
            // 결제 승인 요청 생성
            TossPaymentConfirmRequest confirmRequest = new TossPaymentConfirmRequest(
                    paymentKey, orderId, amount
            );
            
            // 1. 토스페이먼츠 결제 승인 API 호출
            TossPaymentResponse paymentResponse = tossPaymentService.confirmPayment(confirmRequest);
            
            // 2. 결제 상태 확인
            if (!"DONE".equals(paymentResponse.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "결제가 완료되지 않았습니다. 상태: " + paymentResponse.getStatus()));
            }
            
            // 3. 결제 금액 검증
            if (!amount.equals(paymentResponse.getTotalAmount())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "결제 금액이 일치하지 않습니다."));
            }
            
            // 4. 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
            
            // 5. 포인트 충전
            pointService.chargePoint(user, amount);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "포인트 충전이 완료되었습니다.",
                    "paymentKey", paymentResponse.getPaymentKey(),
                    "orderId", paymentResponse.getOrderId(),
                    "amount", paymentResponse.getTotalAmount(),
                    "userPoint", user.getPoint()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

}
