package com.mutsa.springboot_auction.domain.notification.repository;

import com.mutsa.springboot_auction.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    int countByUserIdAndIsReadFalse(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
