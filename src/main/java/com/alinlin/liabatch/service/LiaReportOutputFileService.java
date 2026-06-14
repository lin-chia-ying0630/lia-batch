package com.alinlin.liabatch.service;

import com.alinlin.liabatch.dto.LiaReportData;

import java.nio.file.Path;

/**
 * LIA通報輸出檔 Service 介面。
 * <p>
 * 定義寫入中檔案路徑與正式發布檔案的操作。
 */
public interface LiaReportOutputFileService {
    Path targetPath(String outputArg, LiaReportData reportData);

    Path writingPath(String outputArg, LiaReportData reportData);

    Path publish(Path writingPath, String outputArg, LiaReportData reportData);
}
