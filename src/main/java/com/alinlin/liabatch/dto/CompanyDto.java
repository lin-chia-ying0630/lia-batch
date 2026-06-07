package com.alinlin.liabatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公司資料 DTO。
 * <p>
 * 由 MySQL 的公司來源資料查出，主要用於LIA通報固定欄位與輸出檔命名。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private String companyCode;
}
