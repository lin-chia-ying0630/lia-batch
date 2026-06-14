package com.alinlin.liabatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

/**
 * LIA通報產檔結果 DTO。
 * <p>
 * 保存本次批次實際產生的 TXT、Excel 與 ZIP 檔案路徑，供 Controller 顯示執行結果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiaReportGenerateResult {
    private Path txtPath;
    private Path excelPath;
    private Path zipPath;
    private List<Path> txtPaths;
    private List<Path> excelPaths;
    private List<Path> zipPaths;
}
