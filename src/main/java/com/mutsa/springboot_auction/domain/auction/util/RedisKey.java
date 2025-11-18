package com.mutsa.springboot_auction.domain.auction.util;

public class RedisKey {

    private RedisKey() {
    }
    public static String deckKey(Long userId) {
        return "DECK:" + userId;
    }

    public static String dislikeKey(Long userId) {
        return "DisLike:" + userId;
    }

    public static String holdKey(Long userId) {
        return "HOLD:" + userId;
    }
}
