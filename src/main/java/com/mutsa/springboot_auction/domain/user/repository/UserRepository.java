package com.mutsa.springboot_auction.domain.user.repository;

import com.mutsa.springboot_auction.domain.user.entity.SocialType;
import com.mutsa.springboot_auction.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialId(String socialId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id in :userIds order by u.id")
    List<User> findAllByIdForUpdate(@Param("userIds") Collection<Long> userIds);
}

