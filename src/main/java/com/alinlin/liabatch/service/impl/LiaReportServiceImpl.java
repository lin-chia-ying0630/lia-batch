package com.alinlin.liabatch.service.impl;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;
import com.alinlin.liabatch.dto.LiaReportData;
import com.alinlin.liabatch.repository.LiaReportDataRepository;
import com.alinlin.liabatch.service.LiaReportExcelSpecReader;
import com.alinlin.liabatch.service.LiaReportOutputFileService;
import com.alinlin.liabatch.service.LiaReportService;
import com.alinlin.liabatch.util.FixedLengthTextBuilder;
import com.alinlin.liabatch.util.SpecValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * LIA通報主要 Service 實作。
 * <p>
 * 串接讀取 Excel 規格、檢核規格、查詢 MySQL/mock 資料、組固定長度 TXT、寫入中檔案與正式發布。
 */
@Service
public class LiaReportServiceImpl implements LiaReportService {

    private final String specFile;
    private final LiaReportExcelSpecReader excelSpecReader;
    private final LiaReportDataRepository reportDataRepository;
    private final LiaReportOutputFileService outputFileService;

    @Autowired
    public LiaReportServiceImpl(
            @Value("${lia.report.spec-file:lia-report-spec.xlsx}") String specFile,
            LiaReportExcelSpecReader excelSpecReader,
            LiaReportDataRepository reportDataRepository,
            LiaReportOutputFileService outputFileService
    ) {
        this.specFile = specFile;
        this.excelSpecReader = excelSpecReader;
        this.reportDataRepository = reportDataRepository;
        this.outputFileService = outputFileService;
    }

    @Override
    public Path generate(String outputArg) {
        List<LiaFieldSpecDto> specs = excelSpecReader.read(specFile);
        SpecValidator.validate(specs);

        LiaReportData reportData = reportDataRepository.selectReportData();
        String line = new FixedLengthTextBuilder().buildLine(reportData, specs);

        Path writingPath = outputFileService.writingPath(outputArg, reportData);
        write(writingPath, line);
        return outputFileService.publish(writingPath, outputArg, reportData);
    }

    private void write(Path output, String line) {
        try {
            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }
            Files.writeString(output, line + System.lineSeparator(), Charset.forName("Big5"));
        } catch (IOException e) {
            throw new IllegalStateException("輸出LIA通報暫存檔失敗：" + output, e);
        }
    }
}
