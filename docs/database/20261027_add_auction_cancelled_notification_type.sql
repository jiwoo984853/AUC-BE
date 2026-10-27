ALTER TABLE notification
    MODIFY COLUMN type ENUM(
        'OUTBID',
        'WIN',
        'FAIL',
        'NEXT_WINNER_OFFER',
        'AUCTION_CANCELLED'
    ) NULL;
