package com.alinlin.liabatch.service.impl;

import com.alinlin.liabatch.repository.LiaReportLogRepository;
import com.alinlin.liabatch.service.LiaReportLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * LIA批次執行紀錄 Service 實作。
 * <p>
 * 讀取 console 交給 Java JAR 的 LOG 暫存檔，檢核內容後交由 Repository 寫入 lia_log。
 */
@Service
public class LiaReportLogServiceImpl implements LiaReportLogService {

    private final LiaReportLogRepository liaReportLogRepository;

    @Autowired
    public LiaReportLogServiceImpl(LiaReportLogRepository liaReportLogRepository) {
        this.liaReportLogRepository = liaReportLogRepository;
    }

    @Override
    public void insertLogFromFile(String logContentFile) {
        String content = readContent(logContentFile);
        if (content.isBlank()) {
            throw new IllegalArgumentException("執行紀錄內容不可空白");
        }
        liaReportLogRepository.insertExecutionLog(content);
    }

    private String readContent(String logContentFile) {
        try {
            return Files.readString(Path.of(logContentFile)).trim();
        } catch (IOException e) {
            throw new IllegalStateException("讀取執行紀錄暫存檔失敗：" + logContentFile, e);
        }
    }
}
