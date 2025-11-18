package com.mutsa.springboot_auction.domain.pointHistory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentResponse {
    private String paymentKey;
    private String orderId;
    private String status;
    private Integer totalAmount;
    private String method;
    private String requestedAt;
    private String approvedAt;
}


