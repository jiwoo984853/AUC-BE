package com.mutsa.springboot_auction.domain.qna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QnaCreateQuestionRequestDto {

    @NotNull
    private Long userId;

    @NotBlank
    private String questionContent;
}
