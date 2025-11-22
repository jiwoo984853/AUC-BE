package com.mutsa.springboot_auction.domain.pointHistory.service;

import com.mutsa.springboot_auction.domain.pointHistory.dto.PointHistoryResponse;
import com.mutsa.springboot_auction.domain.pointHistory.entity.HistoryType;
import com.mutsa.springboot_auction.domain.pointHistory.entity.PointHistory;
import com.mutsa.springboot_auction.domain.pointHistory.repository.PointHistoryRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    // user랑 depositAmount 받아서 입찰시 포인트 내역에 저장
    @Transactional
    public void saveDepositHistory(User user, Integer depositAmount) {
        PointHistory history = PointHistory.builder()
                .user(user)
                .type(HistoryType.BID_DEPOSIT)
                .changeAmount(-depositAmount)
                .build();
        pointHistoryRepository.save(history);
    }

    // user랑 refundAmount 받아서 보증금 환불시 포인트 내역에 저장
    @Transactional
    public void saveRefundHistory(User user, Integer refundAmount) {
        PointHistory history = PointHistory.builder()
                .user(user)
                .type(HistoryType.REFUND)
                .changeAmount(refundAmount)
                .build();
        pointHistoryRepository.save(history);
    }

    @Transactional
    public void savePurchaseHistory(User buyer, Integer amount) {
        PointHistory history = PointHistory.builder()
                .user(buyer)
                .changeAmount(-amount)
                .type(HistoryType.PURCHASE)
                .build();
        pointHistoryRepository.save(history);
    }

    @Transactional
    public void saveSaleHistory(User seller, Integer amount) {
        PointHistory history = PointHistory.builder()
                .user(seller)
                .changeAmount(amount)
                .type(HistoryType.SALE)
                .build();
        pointHistoryRepository.save(history);
    }

    public List<PointHistoryResponse> getHistory(User user) {
        List<PointHistory> list = pointHistoryRepository.findByUserOrderByHistoryIdDesc(user);
        return list.stream()
                .map(PointHistoryResponse::of)
                .collect(Collectors.toList());
    }
}
