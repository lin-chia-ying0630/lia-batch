package com.alinlin.liabatch.repository;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * LIA通報執行紀錄 Repository 介面。
 * <p>
 * 定義批次每次觸發時寫入 lia_log 的方法，保存產生日、時間與執行內容。
 */
public interface LiaReportLogRepository {

    void insertLog(LocalDate generateDate, LocalTime generateTime, String content);
}
