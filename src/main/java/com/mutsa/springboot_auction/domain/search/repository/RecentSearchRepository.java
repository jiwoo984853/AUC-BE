package com.mutsa.springboot_auction.domain.search.repository;

import com.mutsa.springboot_auction.domain.search.entity.RecentSearch;
import com.mutsa.springboot_auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {

    List<RecentSearch> findTop10ByUserOrderByCreatedAtDesc(User user);

    RecentSearch findByUserAndKeyword(User user, String keyword);
}
