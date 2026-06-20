SET NAMES utf8mb4;

USE lia_batch;

CREATE TABLE IF NOT EXISTS lia_log (
    log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '執行紀錄流水號',
    generate_date DATE NOT NULL COMMENT '產生日',
    generate_time TIME NOT NULL COMMENT '產生時間',
    content TEXT NOT NULL COMMENT '執行內容',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (log_id),
    KEY idx_lia_log_generate (generate_date, generate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LIA批次執行紀錄';
