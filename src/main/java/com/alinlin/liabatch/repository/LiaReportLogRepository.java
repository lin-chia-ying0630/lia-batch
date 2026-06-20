package com.alinlin.liabatch.repository;

/**
 * LIA批次執行紀錄 Repository 介面。
 * <p>
 * Service 層透過此介面寫入 lia_log，避免直接依賴 MyBatis Mapper。
 */
public interface LiaReportLogRepository {
    void insertExecutionLog(String content);
}
