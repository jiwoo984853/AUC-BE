package com.mutsa.springboot_auction.domain.qna.service;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.qna.dto.QnaAnswerRequestDto;
import com.mutsa.springboot_auction.domain.qna.dto.QnaCreateQuestionRequestDto;
import com.mutsa.springboot_auction.domain.qna.dto.QnaResponseDto;
import com.mutsa.springboot_auction.domain.qna.entity.Qna;
import com.mutsa.springboot_auction.domain.qna.repository.QnaRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnaService {
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;

    @Transactional
    public QnaResponseDto createQuestion(Long auctionId, QnaCreateQuestionRequestDto requestDto) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(()-> new EntityNotFoundException("경매를 찾을 수 없습니다. id=" + auctionId) );

        User questioner = userRepository.findById(requestDto.getUserId())
                .orElseThrow(()-> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + requestDto.getUserId()));

        Qna qna = Qna.builder()
                .auction(auction)
                .questioner(questioner)
                .answerContent(requestDto.getQuestionContent())
                .build();

        Qna saved = qnaRepository.save(qna);
        return toResponseDto(saved);
    }

    @Transactional
    public QnaResponseDto answerQuestion(Long qnaId, QnaAnswerRequestDto requestDto) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(()-> new EntityNotFoundException("QnA를 찾을 수 없습니다. id=" + qnaId) );
        User answerUser = userRepository.findById(requestDto.getUserId())
                .orElseThrow(()-> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + requestDto.getUserId()));

        if (qna.isAnswered()) {
            throw new IllegalArgumentException("이미 답변이 등록된 질문입니다.");
        }
        qna.writeAnswer(answerUser, requestDto.getAnswerContent());
        return toResponseDto(qna);
    }
    @Transactional(readOnly = true)
    public List<QnaResponseDto> getQnaByAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(()-> new EntityNotFoundException("경매를 찾을 수 없습니다. id=" + auctionId) );

        List<Qna> qnaList = qnaRepository.findAllByAuctionOrderByCreatedAtAsc(auction);
        return qnaList.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    private QnaResponseDto toResponseDto(Qna qna) {
        Long answerUserId = null;
        String answerNickname = null;
        String answerContent = null;

        if (qna.getAnswerer() != null) {
            answerUserId = qna.getAnswerer().getId();
            answerNickname = qna.getAnswerer().getNickname();
            answerContent = qna.getAnswerContent();
        }
        return new QnaResponseDto(
                qna.getQnaId(),
                qna.getAuction().getAuctionId(),
                qna.getQuestioner().getId(),
                qna.getQuestioner().getNickname(),
                qna.getQuestionContent(),
                qna.getCreatedAt(),
                answerUserId,
                answerNickname,
                answerContent,
                qna.getAnswerAt()
        );
    }
}
