package com.alinlin.liabatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LIA通報輸出設定 DTO。
 * <p>
 * 對應 Excel 規格檔 outputSettings 工作表展開後的一種輸出設定，第一欄為檔名基底。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiaReportOutputSettingDto {
    private String outputFileName;
    private String outputType;
    private String dataSelectType;
    private String zipPassword;
    private String settingDesc;
}
