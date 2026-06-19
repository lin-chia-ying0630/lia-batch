SET NAMES utf8mb4;

USE lia_batch;

ALTER TABLE lia_policy
    ADD COLUMN mop VARCHAR(10) NULL COMMENT '繳別/繳費方式代碼' AFTER product_code;

UPDATE lia_policy
SET mop = 'A'
WHERE mop IS NULL;
