package com.mutsa.springboot_auction.global.repository;

import com.mutsa.springboot_auction.domain.user.entity.SocialType;
import com.mutsa.springboot_auction.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialIdAndSocialType(String socialId, SocialType socialType);

    boolean existsBySocialIdAndSocialType(String socialId, SocialType socialType);

    Optional<User> findBySocialId(String socialId);
}

