package com.mutsa.springboot_auction.domain.pointHistory.service;

import com.mutsa.springboot_auction.domain.pointHistory.dto.TossPaymentConfirmRequest;
import com.mutsa.springboot_auction.domain.pointHistory.dto.TossPaymentErrorResponse;
import com.mutsa.springboot_auction.domain.pointHistory.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentService {

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    @Value("${toss.payments.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    /**
     * 토스페이먼츠 결제 승인 API 호출
     * 
     * @param request 결제 승인 요청 정보 (paymentKey, orderId, amount)
     * @return TossPaymentResponse 결제 응답 정보
     * @throws RuntimeException 결제 승인 실패 시
     */
    public TossPaymentResponse confirmPayment(TossPaymentConfirmRequest request) {
        String url = baseUrl + "/v1/payments/confirm";
        
        HttpHeaders headers = createHeaders();
        HttpEntity<TossPaymentConfirmRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<TossPaymentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TossPaymentResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("토스페이먼츠 결제 승인 성공: orderId={}, paymentKey={}", 
                        request.getOrderId(), request.getPaymentKey());
                return response.getBody();
            } else {
                throw new RuntimeException("토스페이먼츠 결제 승인 실패: 응답이 비어있습니다.");
            }
        } catch (HttpClientErrorException e) {
            log.error("토스페이먼츠 결제 승인 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            
            // 에러 응답 파싱
            try {
                TossPaymentErrorResponse errorResponse = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(e.getResponseBodyAsString(), TossPaymentErrorResponse.class);
                throw new RuntimeException("토스페이먼츠 결제 승인 실패: " + errorResponse.getMessage());
            } catch (Exception parseException) {
                throw new RuntimeException("토스페이먼츠 결제 승인 실패: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 중 예외 발생", e);
            throw new RuntimeException("토스페이먼츠 결제 승인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 토스페이먼츠 API 호출을 위한 헤더 생성
     * Authorization 헤더에 Base64로 인코딩된 secretKey를 포함
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        
        return headers;
    }
}

