package com.alinlin.liabatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LIA通報欄位規格 DTO。
 * <p>
 * 對應 Excel 規格檔的每一列，描述固定長度 TXT 欄位位置、長度、資料型態與資料來源。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiaFieldSpecDto {
    private Integer sortNo;
    private String targetField;
    private String targetDesc;
    private Integer startPos;
    private Integer endPos;
    private Integer length;
    private String dataType;
    private String sourceFile;
    private String sourceField;
    private String required;
    private String formatRule;
}
