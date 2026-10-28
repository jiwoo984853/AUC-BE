package com.mutsa.springboot_auction.domain.bid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.auction.entity.PaymentStatus;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.bid.dto.BidCreateRequestDto;
import com.mutsa.springboot_auction.domain.bid.repositoy.BidRepository;
import com.mutsa.springboot_auction.domain.notification.service.NotificationService;
import com.mutsa.springboot_auction.domain.pointHistory.service.PointService;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bid-concurrency;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(BidService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class BidServiceConcurrencyTest {

    @Autowired
    private BidService bidService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private PointService pointService;

    @MockitoBean
    private NotificationService notificationService;

    @AfterEach
    void cleanUp() {
        bidRepository.deleteAll();
        auctionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void onlyOneOfTwoSamePriceConcurrentBidsSucceeds() throws Exception {
        User seller = saveUser("seller", "판매자");
        User firstBidder = saveUser("bidder-1", "입찰자1");
        User secondBidder = saveUser("bidder-2", "입찰자2");
        Auction auction = saveAuction(seller);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Boolean> first = executor.submit(
                    () -> placeBidAtTheSameTime(firstBidder.getId(), auction.getAuctionId(), 200, ready, start)
            );
            Future<Boolean> second = executor.submit(
                    () -> placeBidAtTheSameTime(secondBidder.getId(), auction.getAuctionId(), 200, ready, start)
            );

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            long successCount = List.of(first.get(), second.get()).stream()
                    .filter(Boolean::booleanValue)
                    .count();

            assertEquals(1, successCount);
        } finally {
            executor.shutdownNow();
        }

        Auction savedAuction = auctionRepository.findById(auction.getAuctionId()).orElseThrow();
        User savedFirstBidder = userRepository.findById(firstBidder.getId()).orElseThrow();
        User savedSecondBidder = userRepository.findById(secondBidder.getId()).orElseThrow();

        assertEquals(1, bidRepository.count());
        assertEquals(200, savedAuction.getCurrentPrice());
        assertTrue(
                savedAuction.getWinner().getId().equals(firstBidder.getId())
                        || savedAuction.getWinner().getId().equals(secondBidder.getId())
        );
        assertEquals(180, savedFirstBidder.getPoint() + savedSecondBidder.getPoint());
    }

    @Test
    void higherConcurrentBidAlwaysBecomesFinalPriceAndWinner() throws Exception {
        User seller = saveUser("seller-high", "판매자-high");
        User lowerBidder = saveUser("lower-bidder", "낮은입찰자");
        User higherBidder = saveUser("higher-bidder", "높은입찰자");
        Auction auction = saveAuction(seller);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Boolean> lower = executor.submit(
                    () -> placeBidAtTheSameTime(lowerBidder.getId(), auction.getAuctionId(), 200, ready, start)
            );
            Future<Boolean> higher = executor.submit(
                    () -> placeBidAtTheSameTime(higherBidder.getId(), auction.getAuctionId(), 300, ready, start)
            );

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            lower.get();
            assertTrue(higher.get());
        } finally {
            executor.shutdownNow();
        }

        Auction savedAuction = auctionRepository.findById(auction.getAuctionId()).orElseThrow();
        User savedLowerBidder = userRepository.findById(lowerBidder.getId()).orElseThrow();
        User savedHigherBidder = userRepository.findById(higherBidder.getId()).orElseThrow();

        assertEquals(300, savedAuction.getCurrentPrice());
        assertEquals(higherBidder.getId(), savedAuction.getWinner().getId());
        assertEquals(170, savedLowerBidder.getPoint() + savedHigherBidder.getPoint());
    }

    private User saveUser(String socialId, String nickname) {
        return userRepository.saveAndFlush(User.builder()
                .socialId(socialId)
                .nickname(nickname)
                .point(100)
                .build());
    }

    private Auction saveAuction(User seller) {
        return auctionRepository.saveAndFlush(Auction.builder()
                .seller(seller)
                .goodsName("동시 입찰 상품")
                .startPrice(100)
                .currentPrice(100)
                .status(AuctionStatus.IN_PROGRESS)
                .paymentStatus(PaymentStatus.PENDING)
                .startAt(LocalDateTime.now().minusMinutes(1))
                .endAt(LocalDateTime.now().plusMinutes(10))
                .build());
    }

    private boolean placeBidAtTheSameTime(
            Long bidderId,
            Long auctionId,
            int bidPrice,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        ready.countDown();
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                return false;
            }
            bidService.createBid(bidderId, new BidCreateRequestDto(auctionId, bidPrice));
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
