SET NAMES utf8mb4;

USE lia_batch;

CREATE TABLE IF NOT EXISTS lia_product_order (
    product_order_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品訂單流水號',
    policy_no VARCHAR(15) NOT NULL COMMENT '保單號碼',
    policy_seq VARCHAR(5) NOT NULL COMMENT '保單編號序號',
    product_order_no INT NOT NULL COMMENT '商品順序，從1開始依序排列',
    product_code VARCHAR(20) NOT NULL COMMENT '商品代號',
    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '有效註記',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最後更新時間',
    PRIMARY KEY (product_order_id),
    UNIQUE KEY uk_lia_product_order_policy (policy_no, policy_seq, product_order_no),
    KEY idx_lia_product_order_product (product_code),
    KEY idx_lia_product_order_active (active_flag, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LIA商品訂單';

INSERT INTO lia_product_order (
    policy_no,
    policy_seq,
    product_order_no,
    product_code,
    active_flag
) VALUES
(
    'POL123456789',
    '00001',
    1,
    'P001',
    'Y'
),
(
    'POL123456789',
    '00001',
    2,
    'P002',
    'Y'
) ON DUPLICATE KEY UPDATE
    product_code = VALUES(product_code),
    active_flag = VALUES(active_flag),
    updated_at = CURRENT_TIMESTAMP;

