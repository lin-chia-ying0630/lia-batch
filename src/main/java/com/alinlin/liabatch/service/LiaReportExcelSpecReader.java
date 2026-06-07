package com.alinlin.liabatch.service;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;

import java.util.List;

/**
 * LIA通報 Excel 規格讀取介面。
 * <p>
 * 定義讀取 .xlsx 或 .xls 規格檔並轉成欄位規格清單的操作。
 */
public interface LiaReportExcelSpecReader {
    List<LiaFieldSpecDto> read(String classpathLocation);
}
