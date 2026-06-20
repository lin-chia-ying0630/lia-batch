package com.alinlin.liabatch.controller;

import com.alinlin.liabatch.dto.LiaReportGenerateResult;
import com.alinlin.liabatch.repository.LiaReportLogRepository;
import com.alinlin.liabatch.service.LiaReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * LIA通報批次 Controller。
 * <p>
 * 負責解析命令列參數並啟動LIA通報 service，不放業務邏輯。
 */
@Component
public class LiaReportBatchController {

    private final LiaReportService liaReportService;
    private final LiaReportLogRepository liaReportLogRepository;

    @Autowired
    public LiaReportBatchController(
            LiaReportService liaReportService,
            LiaReportLogRepository liaReportLogRepository
    ) {
        this.liaReportService = liaReportService;
        this.liaReportLogRepository = liaReportLogRepository;
    }

    public void run(String[] args) {
        LocalDate generateDate = LocalDate.now();
        LocalTime generateTime = LocalTime.now().withNano(0);
        try {
            LiaReportGenerateResult result = liaReportService.generate(
                    readOutputArg(args),
                    readOutputTypesArg(args),
                    readZipPasswordArg(args)
            );
            result.getTxtPaths().forEach(path -> System.out.println("LIA通報TXT輸出完成：" + path.toAbsolutePath()));
            result.getExcelPaths().forEach(path -> System.out.println("LIA通報Excel輸出完成：" + path.toAbsolutePath()));
            result.getZipPaths().forEach(path -> System.out.println("LIA通報ZIP輸出完成：" + path.toAbsolutePath()));
            insertLog(generateDate, generateTime, successContent(result));
        } catch (RuntimeException e) {
            insertLog(generateDate, generateTime, "執行失敗：" + e.getMessage());
            throw e;
        }
    }

    private void insertLog(LocalDate generateDate, LocalTime generateTime, String content) {
        try {
            liaReportLogRepository.insertLog(generateDate, generateTime, content);
        } catch (RuntimeException e) {
            System.err.println("寫入lia_log失敗：" + e.getMessage());
        }
    }

    private String successContent(LiaReportGenerateResult result) {
        return "執行成功；TXT=" + paths(result.getTxtPaths())
                + "；Excel=" + paths(result.getExcelPaths())
                + "；ZIP=" + paths(result.getZipPaths());
    }

    private String paths(List<Path> paths) {
        if (paths == null || paths.isEmpty()) {
            return "-";
        }
        return paths.stream()
                .map(path -> path.toAbsolutePath().toString())
                .reduce((left, right) -> left + "," + right)
                .orElse("-");
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
