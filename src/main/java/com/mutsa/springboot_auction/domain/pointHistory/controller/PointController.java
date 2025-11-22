package com.mutsa.springboot_auction.domain.pointHistory.controller;

import com.mutsa.springboot_auction.domain.pointHistory.dto.PointChargeRequest;
import com.mutsa.springboot_auction.domain.pointHistory.dto.TossPaymentConfirmRequest;
import com.mutsa.springboot_auction.domain.pointHistory.dto.TossPaymentResponse;
import com.mutsa.springboot_auction.domain.pointHistory.entity.PointHistory;
import com.mutsa.springboot_auction.domain.pointHistory.service.PointService;
import com.mutsa.springboot_auction.domain.pointHistory.service.TossPaymentService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
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
            PointChargeRequest request
    ) {
        User user = oAuth2User.getUser();
        Integer amount = request.getAmount();
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
            if (oAuth2User == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("code", "UNAUTHORIZED", "error", "인증이 필요합니다."));
            }

            User user = oAuth2User.getUser();

            TossPaymentResponse paymentResponse = tossPaymentService.confirmPayment(request);

            if (!"DONE".equals(paymentResponse.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("code", "PAYMENT_NOT_DONE", "error", "결제가 완료되지 않았습니다."));
            }

            if (!request.getAmount().equals(paymentResponse.getTotalAmount())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("code", "AMOUNT_MISMATCH", "error", "결제 금액이 일치하지 않습니다."));
            }

            pointService.chargePoint(user, paymentResponse.getTotalAmount());

            log.info("포인트 충전 성공 - User: {}, Amount: {}", user.getId(), paymentResponse.getTotalAmount());

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
            log.error("결제 승인 중 런타임 오류 발생: User={}, OrderId={}",
                    (oAuth2User != null ? oAuth2User.getUser().getId() : "Anonymous"),
                    request.getOrderId(),
                    e);

            return ResponseEntity.badRequest()
                    .body(Map.of("code", "PAYMENT_FAILED", "error", "결제 승인 중 오류가 발생했습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("code", "SERVER_ERROR", "error", "서버 내부 오류가 발생했습니다."));
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

            String paymentKey = (String) request.get("paymentKey");
            String orderId = (String) request.get("orderId");
            Integer amount = ((Number) request.get("amount")).intValue();
            Long userId = ((Number) request.get("userId")).longValue();

            // 결제 승인 요청 생성
            TossPaymentConfirmRequest confirmRequest = new TossPaymentConfirmRequest(
                    paymentKey, orderId, amount
            );

            TossPaymentResponse paymentResponse = tossPaymentService.confirmPayment(confirmRequest);

            if (!"DONE".equals(paymentResponse.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "결제가 완료되지 않았습니다. 상태: " + paymentResponse.getStatus()));
            }

            if (!amount.equals(paymentResponse.getTotalAmount())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "결제 금액이 일치하지 않습니다."));
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

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
