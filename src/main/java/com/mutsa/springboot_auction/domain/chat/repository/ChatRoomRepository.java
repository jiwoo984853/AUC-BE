package com.mutsa.springboot_auction.domain.chat.repository;

import com.mutsa.springboot_auction.domain.chat.entity.ChatRoom;
import com.mutsa.springboot_auction.domain.auction.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByAuctionAuctionId(Long auctionId);

    @Query("""
            select cr from ChatRoom cr
            join fetch cr.seller
            join fetch cr.buyer
            join fetch cr.auction
            where cr.seller.id = :userId or cr.buyer.id = :userId
            """)
    List<ChatRoom> findAllByParticipantId(@Param("userId") Long userId);

    List<ChatRoom> findAllByAuctionPaymentStatusAndAuctionPaymentCompletedAtBefore(
            PaymentStatus paymentStatus,
            LocalDateTime cutoff
    );
}
