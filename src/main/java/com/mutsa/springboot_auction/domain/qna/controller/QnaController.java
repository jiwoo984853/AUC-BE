package com.mutsa.springboot_auction.domain.qna.controller;


import com.mutsa.springboot_auction.domain.qna.dto.QnaAnswerRequestDto;
import com.mutsa.springboot_auction.domain.qna.dto.QnaCreateQuestionRequestDto;
import com.mutsa.springboot_auction.domain.qna.dto.QnaResponseDto;
import com.mutsa.springboot_auction.domain.qna.service.QnaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QnaController {
    private final QnaService qnaService;

    @PostMapping("/auctions/{auctionId}/qna")
    public ResponseEntity<QnaResponseDto> createQuestion(
            @PathVariable Long auctionId,
            @Valid @RequestBody QnaCreateQuestionRequestDto requestDto
    ){
        QnaResponseDto responseDto = qnaService.createQuestion(auctionId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/qna/{qnaId}/answer")
    public ResponseEntity<QnaResponseDto> answerQuestion(
            @PathVariable Long qnaId,
            @Valid @RequestBody QnaAnswerRequestDto requestDto
    ){
        QnaResponseDto responseDto = qnaService.answerQuestion(qnaId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/auctions/{auctionId}/qna")
    public ResponseEntity<List<QnaResponseDto>> getQnaByAuction(@PathVariable Long auctionId) {
        List<QnaResponseDto> qnaList = qnaService.getQnaByAuction(auctionId);
        return ResponseEntity.ok(qnaList);
    }
}
