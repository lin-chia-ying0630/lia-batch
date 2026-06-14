package com.alinlin.liabatch.service;

import com.alinlin.liabatch.dto.LiaReportSpecDto;

/**
 * LIA通報 Excel 規格讀取介面。
 * <p>
 * 定義讀取 .xlsx 或 .xls 規格檔並轉成欄位規格與輸出設定的操作。
 */
public interface LiaReportExcelSpecReader {
    LiaReportSpecDto read(String classpathLocation);
}
