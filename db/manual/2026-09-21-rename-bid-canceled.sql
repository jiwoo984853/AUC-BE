-- Apply to the selected MySQL database before deploying code that removes BidStatus.CANCELED.
-- The column may be a native ENUM or VARCHAR, depending on how its schema was created.
-- Converting it to VARCHAR first allows the new value in either case.
-- Back up the Bid table and run the pre-check before applying this migration.

SELECT DATA_TYPE, COLUMN_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'Bid'
  AND COLUMN_NAME = 'bid_status';

SELECT bid_status, COUNT(*) AS rows_with_status
FROM `Bid`
GROUP BY bid_status;

ALTER TABLE `Bid` MODIFY COLUMN `bid_status` VARCHAR(32) NULL;

UPDATE `Bid`
SET `bid_status` = 'CANCELLED'
WHERE `bid_status` = 'CANCELED';

SELECT bid_status, COUNT(*) AS rows_with_status
FROM `Bid`
GROUP BY bid_status;

SELECT COUNT(*) AS remaining_legacy_rows
FROM `Bid`
WHERE `bid_status` = 'CANCELED';
