ALTER TABLE auction
    ADD COLUMN deleted_at DATETIME(6) NULL;

CREATE INDEX idx_auction_deleted_at
    ON auction (deleted_at);
