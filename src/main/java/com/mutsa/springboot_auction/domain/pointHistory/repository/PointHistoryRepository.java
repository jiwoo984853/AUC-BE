package com.mutsa.springboot_auction.domain.pointHistory.repository;

import com.mutsa.springboot_auction.domain.pointHistory.entity.PointHistory;
import com.mutsa.springboot_auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByUserOrderByHistoryIdDesc(User user);
}
