SET NAMES utf8mb4;

USE lia_batch;

INSERT INTO lia_company (company_code, company_name, active_flag)
VALUES ('01', '公司', 'Y')
ON DUPLICATE KEY UPDATE
    company_name = VALUES(company_name),
    active_flag = VALUES(active_flag),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO lia_policy (
    company_code,
    policy_no,
    policy_seq,
    change_seq,
    insured_amount,
    active_flag
) VALUES (
    '01',
    'POL123456789',
    '00001',
    '1',
    1000000,
    'Y'
);

INSERT INTO lia_customer (
    customer_id,
    customer_name,
    customer_type,
    id_type,
    id_no,
    active_flag
) VALUES (
    'A123456789',
    '王小明',
    'P',
    '1',
    'A123456789',
    'Y'
) ON DUPLICATE KEY UPDATE
    customer_name = VALUES(customer_name),
    customer_type = VALUES(customer_type),
    id_type = VALUES(id_type),
    id_no = VALUES(id_no),
    active_flag = VALUES(active_flag),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO lia_product (
    product_code,
    product_name,
    active_flag
) VALUES (
    'P001',
    '終身壽險商品',
    'Y'
) ON DUPLICATE KEY UPDATE
    product_name = VALUES(product_name),
    active_flag = VALUES(active_flag),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO lia_payment (
    pay_period_type,
    pay_period,
    active_flag
) VALUES (
    '1',
    '020',
    'Y'
);
