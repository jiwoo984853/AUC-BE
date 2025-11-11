package com.mutsa.springboot_auction.domain.chat.repository;

import com.mutsa.springboot_auction.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByAuctionAuctionId(Long auctionId);
}
