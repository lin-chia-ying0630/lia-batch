CREATE DATABASE IF NOT EXISTS lia_batch
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE lia_batch;

ALTER DATABASE lia_batch
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lia_policy (
    policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '保單資料流水號',
    company_code VARCHAR(2) NOT NULL COMMENT '公司代號',
    policy_no VARCHAR(15) NOT NULL COMMENT '保單號碼',
    policy_seq VARCHAR(5) NOT NULL COMMENT '保單編號序號',
    change_seq VARCHAR(5) NOT NULL COMMENT '異動序號',
    product_code VARCHAR(20) NOT NULL COMMENT '購買商品代號',
    mop VARCHAR(10) NULL COMMENT '繳別/繳費方式代碼',
    insured_amount DECIMAL(18, 0) NOT NULL COMMENT '保險金額',
    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '有效註記',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最後更新時間',
    PRIMARY KEY (policy_id),
    KEY idx_lia_policy_active (active_flag, updated_at),
    KEY idx_lia_policy_product (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LIA保單資料';

CREATE TABLE IF NOT EXISTS lia_customer (
    customer_id VARCHAR(20) NOT NULL COMMENT '客戶代號',
    customer_name VARCHAR(100) NULL COMMENT '客戶中文姓名',
    customer_type VARCHAR(1) NULL COMMENT '客戶類型',
    id_type VARCHAR(1) NOT NULL COMMENT '證件類型',
    id_no VARCHAR(10) NOT NULL COMMENT '證件號碼',
    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '有效註記',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最後更新時間',
    PRIMARY KEY (customer_id),
    KEY idx_lia_customer_active (active_flag, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LIA客戶資料';

CREATE TABLE IF NOT EXISTS lia_product (
    product_code VARCHAR(20) NOT NULL COMMENT '商品代號',
    product_name VARCHAR(100) NULL COMMENT '商品中文名稱',
    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '有效註記',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最後更新時間',
    PRIMARY KEY (product_code),
    KEY idx_lia_product_active (active_flag, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LIA商品資料';

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

CREATE TABLE IF NOT EXISTS lia_payment (
    payment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '繳費資料流水號',
    policy_id BIGINT NOT NULL COMMENT '保單資料流水號，對應lia_policy.policy_id',
    pay_period_type VARCHAR(1) NOT NULL COMMENT '繳費期間別',
    pay_period VARCHAR(3) NOT NULL COMMENT '繳費期間',
    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '有效註記',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最後更新時間',
    PRIMARY KEY (payment_id),
    KEY idx_lia_payment_policy (policy_id, active_flag, updated_at),
    KEY idx_lia_payment_active (active_flag, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LIA繳費資料';

CREATE TABLE IF NOT EXISTS lia_log (
    log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '執行紀錄流水號',
    generate_date DATE NOT NULL COMMENT '產生日',
    generate_time TIME NOT NULL COMMENT '產生時間',
    content TEXT NOT NULL COMMENT '執行內容',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (log_id),
    KEY idx_lia_log_generate (generate_date, generate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LIA批次執行紀錄';

ALTER TABLE lia_policy CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE lia_customer CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE lia_product CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE lia_product_order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE lia_payment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE lia_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
