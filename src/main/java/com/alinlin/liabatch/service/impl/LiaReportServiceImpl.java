package com.alinlin.liabatch.service.impl;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;
import com.alinlin.liabatch.dto.LiaReportData;
import com.alinlin.liabatch.dto.LiaReportGenerateResult;
import com.alinlin.liabatch.dto.LiaReportOutputSettingDto;
import com.alinlin.liabatch.enumvalue.LiaReportOutputType;
import com.alinlin.liabatch.dto.LiaReportSpecDto;
import com.alinlin.liabatch.repository.LiaReportDataRepository;
import com.alinlin.liabatch.service.LiaReportExcelSpecReader;
import com.alinlin.liabatch.service.LiaReportOutputFileService;
import com.alinlin.liabatch.service.LiaReportService;
import com.alinlin.liabatch.util.FixedLengthTextBuilder;
import com.alinlin.liabatch.util.LiaReportExcelOutputUtil;
import com.alinlin.liabatch.util.LiaReportTxtOutputUtil;
import com.alinlin.liabatch.util.LiaReportZipOutputUtil;
import com.alinlin.liabatch.util.SpecValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LIA通報主要 Service 實作。
 * <p>
 * 串接讀取 Excel 規格、檢核規格、查詢 MySQL 資料、組固定長度 TXT、發布正式檔與產生 Excel 檢視檔。
 */
@Service
public class LiaReportServiceImpl implements LiaReportService {

    private static final String SELECT_REPORT_DATA = "1";
    private static final String SELECT_PRODUCT_ORDER_REPORT_DATA = "2";

    private final String specFile;
    private final LiaReportExcelSpecReader excelSpecReader;
    private final LiaReportDataRepository reportDataRepository;
    private final LiaReportOutputFileService outputFileService;
    private final LiaReportTxtOutputUtil txtOutputUtil = new LiaReportTxtOutputUtil();
    private final LiaReportExcelOutputUtil excelOutputUtil = new LiaReportExcelOutputUtil();
    private final LiaReportZipOutputUtil zipOutputUtil = new LiaReportZipOutputUtil();

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
    public LiaReportGenerateResult generate(String outputArg, String outputTypesArg, String zipPassword) {
        LiaReportSpecDto reportSpec = excelSpecReader.read(specFile);
        String resolvedOutputTypesArg = choose(outputTypesArg, reportSpec.getOutputTypes());
        if (resolvedOutputTypesArg.isBlank()) {
            throw new IllegalArgumentException("outputSettings 至少需要 outputFileTxt、outputFileExcel 或 outputFileZip 其中一欄填 V");
        }
        Set<LiaReportOutputType> commandOutputTypes = outputTypesArg == null || outputTypesArg.isBlank()
                ? Set.of()
                : LiaReportOutputType.parse(outputTypesArg);
        List<LiaFieldSpecDto> specs = reportSpec.getFieldSpecs();
        SpecValidator.validateByOutputFile(specs);

        LiaReportData reportData = reportDataRepository.selectReportData();
        Map<String, List<LiaFieldSpecDto>> specsByOutputFile = specsByOutputFile(specs);
        Map<String, List<LiaReportOutputSettingDto>> settingsByOutputFile = settingsByOutputFile(reportSpec);
        Path outputDirectory = outputDirectory(outputArg, reportData);

        List<Path> txtPaths = new ArrayList<>();
        List<Path> excelPaths = new ArrayList<>();
        List<Path> zipPaths = new ArrayList<>();

        for (Map.Entry<String, List<LiaReportOutputSettingDto>> entry : settingsByOutputFile.entrySet()) {
            String outputFileName = entry.getKey();
            List<LiaReportOutputSettingDto> outputSettings = entry.getValue();
            List<LiaFieldSpecDto> fileSpecs = specsByOutputFile.get(outputFileName);
            if (fileSpecs == null || fileSpecs.isEmpty()) {
                throw new IllegalArgumentException("outputFileDetail 找不到檔案名稱對應欄位：" + outputFileName);
            }

            Set<LiaReportOutputType> outputTypes = outputTypes(outputSettings, commandOutputTypes);
            String resolvedZipPassword = choose(zipPassword, zipPassword(outputSettings));
            List<LiaReportData> reportDataList = reportDataList(outputSettings, reportData);
            List<String> lines = outputTypes.contains(LiaReportOutputType.TXT) || outputTypes.contains(LiaReportOutputType.ZIP)
                    ? buildLines(reportDataList, fileSpecs)
                    : null;

            Path plannedTxtPath = outputDirectory.resolve(withExtension(outputFileName, ".txt"));
            Path plannedExcelPath = outputDirectory.resolve(withExtension(outputFileName, ".xlsx"));

            if (outputTypes.contains(LiaReportOutputType.TXT)) {
                Path writingPath = plannedTxtPath.resolveSibling(plannedTxtPath.getFileName() + ".writing");
                txtOutputUtil.write(writingPath, lines);
                txtPaths.add(publish(writingPath, plannedTxtPath));
            }

            if (outputTypes.contains(LiaReportOutputType.EXCEL)) {
                excelOutputUtil.write(plannedExcelPath, reportDataList, fileSpecs);
                excelPaths.add(plannedExcelPath);
            }

            if (outputTypes.contains(LiaReportOutputType.ZIP)) {
                Path zipPath = outputDirectory.resolve(withExtension(outputFileName, ".zip"));
                zipOutputUtil.write(zipPath, zipEntries(plannedTxtPath, plannedExcelPath, reportDataList, fileSpecs, lines), resolvedZipPassword);
                zipPaths.add(zipPath);
            }
        }

        return LiaReportGenerateResult.builder()
                .txtPath(firstOrNull(txtPaths))
                .excelPath(firstOrNull(excelPaths))
                .zipPath(firstOrNull(zipPaths))
                .txtPaths(txtPaths)
                .excelPaths(excelPaths)
                .zipPaths(zipPaths)
                .build();
    }

    private Path publish(Path writingPath, Path targetPath) {
        try {
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }
            return Files.move(writingPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("發布LIA通報檔失敗：" + writingPath + " -> " + targetPath, e);
        }
    }

    private String choose(String commandValue, String specValue) {
        if (commandValue != null && !commandValue.isBlank()) {
            return commandValue;
        }
        return specValue == null ? "" : specValue;
    }

    private Map<String, byte[]> zipEntries(
            Path txtPath,
            Path excelPath,
            List<LiaReportData> reportDataList,
            List<LiaFieldSpecDto> specs,
            List<String> lines
    ) {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put(txtPath.getFileName().toString(), txtOutputUtil.toBytes(lines));
        entries.put(excelPath.getFileName().toString(), excelOutputUtil.toBytes(sheetName(excelPath), reportDataList, specs));
        return entries;
    }

    private List<LiaReportData> reportDataList(List<LiaReportOutputSettingDto> outputSettings, LiaReportData defaultReportData) {
        String dataSelectType = dataSelectType(outputSettings);
        if (SELECT_PRODUCT_ORDER_REPORT_DATA.equals(dataSelectType)) {
            return reportDataRepository.selectProductOrderReportData();
        }
        if (!SELECT_REPORT_DATA.equals(dataSelectType)) {
            throw new IllegalArgumentException("outputSettings.dataSelectType 只支援 1 或 2，目前值：" + dataSelectType);
        }
        return List.of(defaultReportData);
    }

    private String dataSelectType(List<LiaReportOutputSettingDto> outputSettings) {
        return outputSettings.stream()
                .map(LiaReportOutputSettingDto::getDataSelectType)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(SELECT_REPORT_DATA);
    }

    private List<String> buildLines(List<LiaReportData> reportDataList, List<LiaFieldSpecDto> fileSpecs) {
        FixedLengthTextBuilder textBuilder = new FixedLengthTextBuilder();
        return reportDataList.stream()
                .map(reportData -> textBuilder.buildLine(reportData, fileSpecs))
                .toList();
    }

    private String withExtension(String fileName, String extension) {
        return fileName.toLowerCase().endsWith(extension) ? fileName : fileName + extension;
    }

    private String sheetName(Path excelPath) {
        String fileName = excelPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private Path outputDirectory(String outputArg, LiaReportData reportData) {
        Path defaultTargetPath = outputFileService.targetPath(outputArg, reportData);
        Path parent = defaultTargetPath.getParent();
        return parent == null ? Path.of(".") : parent;
    }

    private Map<String, List<LiaFieldSpecDto>> specsByOutputFile(List<LiaFieldSpecDto> specs) {
        return specs.stream()
                .collect(Collectors.groupingBy(spec -> spec.getOutputFileName().trim(), LinkedHashMap::new, Collectors.toList()));
    }

    private Map<String, List<LiaReportOutputSettingDto>> settingsByOutputFile(LiaReportSpecDto reportSpec) {
        if (reportSpec.getOutputSettings() == null || reportSpec.getOutputSettings().isEmpty()) {
            throw new IllegalArgumentException("outputSettings 至少需要一列設定");
        }
        return reportSpec.getOutputSettings().stream()
                .collect(Collectors.groupingBy(LiaReportOutputSettingDto::getOutputFileName, LinkedHashMap::new, Collectors.toList()));
    }

    private Set<LiaReportOutputType> outputTypes(
            List<LiaReportOutputSettingDto> outputSettings,
            Set<LiaReportOutputType> commandOutputTypes
    ) {
        Set<LiaReportOutputType> enabledTypes = outputSettings.stream()
                .map(setting -> LiaReportOutputType.valueOf(setting.getOutputType().toUpperCase()))
                .collect(Collectors.toSet());
        if (commandOutputTypes == null || commandOutputTypes.isEmpty()) {
            return enabledTypes;
        }
        enabledTypes.retainAll(commandOutputTypes);
        return enabledTypes;
    }

    private String zipPassword(List<LiaReportOutputSettingDto> outputSettings) {
        return outputSettings.stream()
                .filter(setting -> LiaReportOutputType.ZIP.name().equalsIgnoreCase(setting.getOutputType()))
                .map(LiaReportOutputSettingDto::getZipPassword)
                .filter(password -> password != null && !password.isBlank())
                .findFirst()
                .orElse("");
    }

    private Path firstOrNull(List<Path> paths) {
        return paths.isEmpty() ? null : paths.get(0);
    }
}
