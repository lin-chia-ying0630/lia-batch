package com.alinlin.liabatch.service;

/**
 * LIA批次執行紀錄 Service 介面。
 * <p>
 * 提供 Java JAR 的 log-only 模式使用，將外部暫存檔中的執行紀錄寫入 lia_log。
 */
public interface LiaReportLogService {
    void insertLogFromFile(String logContentFile);
}
