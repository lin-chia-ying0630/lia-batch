package com.alinlin.liabatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * LIA通報 Excel 規格 DTO。
 * <p>
 * 保存固定長度欄位規格，以及同一份 Excel 規格檔內的產檔設定。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiaReportSpecDto {
    private List<LiaFieldSpecDto> fieldSpecs;
    private String outputTypes;
    private String zipPassword;
    private List<LiaReportOutputSettingDto> outputSettings;
    private Map<String, String> codeTable;
}
