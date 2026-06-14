package com.alinlin.liabatch.service;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;
import com.alinlin.liabatch.dto.LiaReportData;

import java.nio.file.Path;
import java.util.List;

/**
 * LIA通報 Excel 輸出 Service 介面。
 * <p>
 * 依欄位規格與來源資料產生便於人工檢視的 Excel 檔，第一列表頭使用中文欄位說明。
 */
public interface LiaReportExcelOutputService {
    Path write(Path txtOutputPath, LiaReportData reportData, List<LiaFieldSpecDto> specs);
}
