package com.mutsa.springboot_auction.domain.qna.repository;


import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.qna.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaRepository extends JpaRepository<Qna, Long> {
    List<Qna> findAllByAuctionOrderByCreatedAtAsc(Auction auction);
}
