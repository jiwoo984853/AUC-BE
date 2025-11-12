package com.mutsa.springboot_auction.domain.pointHistory.service;

import com.mutsa.springboot_auction.domain.pointHistory.entity.PointHistory;
import com.mutsa.springboot_auction.domain.pointHistory.repository.PointHistoryRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mutsa.springboot_auction.domain.pointHistory.entity.HistoryType.CHARGE;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void chargePoint(User user, Integer amount) {
        user.chargePoint(amount);

        PointHistory history = PointHistory.builder()
                .user(user)
                .changeAmount(amount)
                .type(CHARGE)
                .build();
        pointHistoryRepository.save(history);
    }

    public List<PointHistory> getHistory(User user) {
        return pointHistoryRepository.findByUserOrderByHistoryIdDesc(user);
    }
}
