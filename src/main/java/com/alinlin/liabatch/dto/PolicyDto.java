package com.alinlin.liabatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 保單資料 DTO。
 * <p>
 * 由 MySQL 的保單來源資料查出，供LIA通報規格中 sourceFile=POLICY 的欄位取值使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDto {
    private String companyCode;
    private String policyNo;
    private String policySeq;
    private String changeSeq;
    private BigDecimal insuredAmount;
}
