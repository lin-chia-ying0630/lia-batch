SET NAMES utf8mb4;

USE lia_batch;

ALTER TABLE lia_payment
    ADD COLUMN policy_id BIGINT NULL COMMENT '保單資料流水號，對應lia_policy.policy_id' AFTER payment_id,
    ADD KEY idx_lia_payment_policy (policy_id, active_flag, updated_at);

UPDATE lia_payment
SET policy_id = 1
WHERE policy_id IS NULL;

ALTER TABLE lia_payment
    MODIFY COLUMN policy_id BIGINT NOT NULL COMMENT '保單資料流水號，對應lia_policy.policy_id';
