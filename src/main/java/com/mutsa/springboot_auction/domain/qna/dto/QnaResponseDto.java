package com.mutsa.springboot_auction.domain.qna.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QnaResponseDto {

    private Long qnaId;
    private Long auctionId;

    private Long questionUserId;
    private String questionUserNickname;
    private String questionUserProfileImageUrl;
    private String questionContent;
    private LocalDateTime questionCreatedAt;

    private Long answerUserId;
    private String answerUserNickname;
    private String answerUserProfileImageUrl;
    private String answerContent;
    private LocalDateTime answerCreatedAt;

}
