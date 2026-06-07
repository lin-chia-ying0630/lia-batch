package com.alinlin.liabatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客戶資料 DTO。
 * <p>
 * 由 MySQL 的客戶來源資料查出，供LIA通報規格中 sourceFile=CUSTOMER 的欄位取值使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private String customerId;
    private String customerName;
    private String customerType;
    private String idType;
    private String idNo;
}
