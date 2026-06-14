package com.alinlin.liabatch.service.impl;

import com.alinlin.liabatch.dto.CompanyDto;
import com.alinlin.liabatch.dto.LiaReportData;
import com.alinlin.liabatch.service.LiaReportOutputFileService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LIA通報輸出檔 Service 實作。
 * <p>
 * 負責建立寫入中檔案路徑與正式檔名，確保資料先寫入 .writing 檔，再發布成正式 .txt。
 */
@Service
public class LiaReportOutputFileServiceImpl implements LiaReportOutputFileService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public Path writingPath(String outputArg, LiaReportData reportData) {
        Path targetPath = targetPath(outputArg, reportData);
        return targetPath.resolveSibling(targetPath.getFileName() + ".writing");
    }

    @Override
    public Path publish(Path writingPath, String outputArg, LiaReportData reportData) {
        Path targetPath = targetPath(outputArg, reportData);
        try {
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }
            return Files.move(writingPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("發布LIA通報檔失敗：" + writingPath + " -> " + targetPath, e);
        }
    }

    @Override
    public Path targetPath(String outputArg, LiaReportData reportData) {
        Path output = Path.of(outputArg);
        if (looksLikeDirectory(outputArg)) {
            return output.resolve(defaultFileName(reportData));
        }
        return output;
    }

    private boolean looksLikeDirectory(String outputArg) {
        return outputArg.endsWith("/") || outputArg.endsWith("\\") || !Path.of(outputArg).getFileName().toString().contains(".");
    }

    private String defaultFileName(LiaReportData reportData) {
        CompanyDto company = reportData.getCompany();
        String companyCode = company == null || company.getCompanyCode() == null
                ? "NA"
                : company.getCompanyCode();
        return "LIA_REPORT_" + companyCode + "_" + LocalDateTime.now().format(FORMATTER) + ".txt";
    }
}
