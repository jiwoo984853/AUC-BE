package com.mutsa.springboot_auction.domain.auction.dto;


import com.mutsa.springboot_auction.domain.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RefuseToPurchaseResponse {
    private Boolean success;
}
