package com.alinlin.liabatch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 繳費資料 DTO。
 * <p>
 * 由 MySQL 的繳費來源資料查出，供LIA通報規格中 sourceFile=PAYMENT 的欄位取值使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long policyId;
    private String payPeriodType;
    private String payPeriod;
}
