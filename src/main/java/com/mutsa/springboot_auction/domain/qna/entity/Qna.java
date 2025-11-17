package com.mutsa.springboot_auction.domain.qna.entity;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "auction_qna")
public class Qna extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_id")
    private Long qnaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id",nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id",nullable = false)
    private User questioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id",nullable = false)
    private User answerer;

    @Column(name = "question_content",nullable = false,length = 1000)
    private String questionContent;

    @Column(name = "answer_content",length = 1000)
    private String answerContent;

    @Column(name = "answer_at")
    private LocalDateTime answerAt;

    public void writeAnswer(User seller,String answer){
        this.answerer=seller;
        this.answerContent=answer;
        this.answerAt=LocalDateTime.now();
    }

    public boolean isAnswered(){
        return this.answerContent!=null;
    }

}
