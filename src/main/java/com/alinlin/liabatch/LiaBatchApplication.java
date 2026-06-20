package com.alinlin.liabatch;

import com.alinlin.liabatch.controller.LiaReportBatchController;
import com.alinlin.liabatch.service.LiaReportLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

/**
 * LIA 批次程式啟動入口。
 * <p>
 * Spring Boot 啟動後透過 CommandLineRunner 將命令列參數交給LIA通報批次 controller。
 */
@SpringBootApplication
public class LiaBatchApplication implements CommandLineRunner {

    private static final String LOG_CONTENT_FILE_ARG = "--log-content-file=";

    private final LiaReportBatchController liaReportBatchController;
    private final LiaReportLogService liaReportLogService;

    @Autowired
    public LiaBatchApplication(
            LiaReportBatchController liaReportBatchController,
            LiaReportLogService liaReportLogService
    ) {
        this.liaReportBatchController = liaReportBatchController;
        this.liaReportLogService = liaReportLogService;
    }

    public static void main(String[] args) {
        SpringApplication.run(LiaBatchApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String logContentFile = logContentFile(args);
        if (!logContentFile.isBlank()) {
            liaReportLogService.insertLogFromFile(logContentFile);
            return;
        }
        liaReportBatchController.run(args);
    }

    private String logContentFile(String... args) {
        return Arrays.stream(args)
                .filter(arg -> arg != null && arg.startsWith(LOG_CONTENT_FILE_ARG))
                .map(arg -> arg.substring(LOG_CONTENT_FILE_ARG.length()).trim())
                .findFirst()
                .orElse("");
    }
}
