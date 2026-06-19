package com.alinlin.liabatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代碼轉換表 DTO。
 * <p>
 * 對應 lia-report-spec.xlsx 的 codeTable 工作表，用 replaceGroup、sourceField 與 sourceValue
 * 找到要輸出的 targetValue。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeTableDto {
    private String replaceGroup;
    private String sourceField;
    private String sourceValue;
    private String targetValue;
    private String codeDesc;
}
