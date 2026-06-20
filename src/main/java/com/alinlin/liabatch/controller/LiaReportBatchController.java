package com.alinlin.liabatch.controller;

import com.alinlin.liabatch.dto.LiaReportGenerateResult;
import com.alinlin.liabatch.service.LiaReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        LiaReportGenerateResult result = liaReportService.generate(
                readOutputArg(args),
                readOutputTypesArg(args),
                readZipPasswordArg(args)
        );
        result.getTxtPaths().forEach(path -> System.out.println("LIA通報TXT輸出完成：" + path.toAbsolutePath()));
        result.getExcelPaths().forEach(path -> System.out.println("LIA通報Excel輸出完成：" + path.toAbsolutePath()));
        result.getZipPaths().forEach(path -> System.out.println("LIA通報ZIP輸出完成：" + path.toAbsolutePath()));
    }

    private String readOutputArg(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--output=")) {
                return arg.substring("--output=".length());
            }
        }
        return "target/lia-report.txt";
    }

    private String readOutputTypesArg(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--output-types=")) {
                return arg.substring("--output-types=".length());
            }
        }
        return "";
    }

    private String readZipPasswordArg(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--zip-password=")) {
                return arg.substring("--zip-password=".length());
            }
        }
        return "";
    }
}
