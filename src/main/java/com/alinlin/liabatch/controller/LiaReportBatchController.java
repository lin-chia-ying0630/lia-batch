package com.alinlin.liabatch.controller;

import com.alinlin.liabatch.service.LiaReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * LIA通報批次 Controller。
 * <p>
 * 負責解析命令列參數並啟動LIA通報 service，不放業務邏輯。
 */
@Component
public class LiaReportBatchController {

    private final LiaReportService liaReportService;

    @Autowired
    public LiaReportBatchController(LiaReportService liaReportService) {
        this.liaReportService = liaReportService;
    }

    public void run(String[] args) {
        Path output = liaReportService.generate(readOutputArg(args));
        System.out.println("LIA通報檔輸出完成：" + output.toAbsolutePath());
    }

    private String readOutputArg(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--output=")) {
                return arg.substring("--output=".length());
            }
        }
        return "target/lia-report.txt";
    }
}
