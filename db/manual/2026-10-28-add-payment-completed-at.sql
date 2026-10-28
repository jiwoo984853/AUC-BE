-- Run this migration before deploying the chat-retention code.
-- Existing completed transactions start a fresh 90-day retention period at migration time.

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'auction'
  AND COLUMN_NAME = 'payment_completed_at';

ALTER TABLE auction
    ADD COLUMN payment_completed_at DATETIME(6) NULL;

UPDATE auction
SET payment_completed_at = NOW(6)
WHERE payment_status = 'COMPLETED'
  AND payment_completed_at IS NULL;

CREATE INDEX idx_auction_payment_completed_at
    ON auction (payment_completed_at);

SELECT payment_status, COUNT(*) AS auction_count,
       MIN(payment_completed_at) AS oldest_completion,
       MAX(payment_completed_at) AS newest_completion
FROM auction
GROUP BY payment_status;
