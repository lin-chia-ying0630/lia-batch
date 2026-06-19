package com.alinlin.liabatch.service.impl;

import com.alinlin.liabatch.dto.CodeTableDto;
import com.alinlin.liabatch.dto.LiaFieldSpecDto;
import com.alinlin.liabatch.dto.LiaReportOutputSettingDto;
import com.alinlin.liabatch.dto.LiaReportSpecDto;
import com.alinlin.liabatch.service.LiaReportExcelSpecReader;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LIA通報 Excel 規格讀取器實作。
 * <p>
 * 使用 Apache POI 讀取 classpath 內的 .xlsx 或 .xls 規格檔，並轉成欄位規格與輸出設定。
 */
@Component
public class LiaReportExcelSpecReaderImpl implements LiaReportExcelSpecReader {

    private final DataFormatter dataFormatter = new DataFormatter();

    @Override
    public LiaReportSpecDto read(String classpathLocation) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(classpathLocation)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("找不到規格檔：" + classpathLocation);
            }

            try (Workbook workbook = WorkbookFactory.create(inputStream)) {
                List<LiaReportOutputSettingDto> outputSettings = parseOutputSettings(workbook.getSheet("outputSettings"));
                Map<String, String> codeTable = parseCodeTable(workbook.getSheet("codeTable"));
                return LiaReportSpecDto.builder()
                        .fieldSpecs(parseFieldSpecSheet(fieldSpecSheet(workbook)))
                        .outputTypes(toOutputTypes(outputSettings))
                        .zipPassword(zipPassword(outputSettings))
                        .outputSettings(outputSettings)
                        .codeTable(codeTable)
                        .build();
            }
        } catch (IOException e) {
            throw new IllegalStateException("讀取規格檔失敗：" + classpathLocation, e);
        }
    }

    private Sheet fieldSpecSheet(Workbook workbook) {
        Sheet sheet = workbook.getSheet("outputFileDetail");
        return sheet == null ? workbook.getSheetAt(0) : sheet;
    }

    private List<LiaFieldSpecDto> parseFieldSpecSheet(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return List.of();
        }

        Map<String, Integer> headers = toHeaderMap(headerRow);
        List<LiaFieldSpecDto> specs = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isBlank(value(row, headers, "targetField"))) {
                continue;
            }

            specs.add(LiaFieldSpecDto.builder()
                    .outputFileName(value(row, headers, "outputFileName"))
                    .sortNo(toInt(value(row, headers, "sortNo")))
                    .targetField(value(row, headers, "targetField"))
                    .targetDesc(value(row, headers, "targetDesc"))
                    .startPos(toInt(value(row, headers, "startPos")))
                    .endPos(toInt(value(row, headers, "endPos")))
                    .length(toInt(value(row, headers, "length")))
                    .dataType(value(row, headers, "dataType"))
                    .sourceFile(value(row, headers, "sourceFile"))
                    .sourceField(value(row, headers, "sourceField"))
                    .replaceGroup(outputReplaceGroup(row, headers))
                    .fixedValue(value(row, headers, "fixedValue"))
                    .required(value(row, headers, "required"))
                    .decimalPlaces(toInt(value(row, headers, "decimalPlaces")))
                    .build());
        }
        return specs;
    }

    private Map<String, String> parseCodeTable(Sheet sheet) {
        if (sheet == null || sheet.getRow(0) == null) {
            return Map.of();
        }

        Map<String, Integer> headers = toHeaderMap(sheet.getRow(0));
        List<CodeTableDto> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isBlank(outputReplaceGroup(row, headers))) {
                continue;
            }
            rows.add(CodeTableDto.builder()
                    .replaceGroup(outputReplaceGroup(row, headers))
                    .sourceField(valueByAliases(row, headers, "sourceField", "source_field"))
                    .sourceValue(valueByAliases(row, headers, "sourceValue", "source_value"))
                    .targetValue(valueByAliases(row, headers, "targetValue", "target_value"))
                    .codeDesc(optionalValue(row, headers, "codeDesc"))
                    .build());
        }
        return rows.stream()
                .collect(Collectors.toMap(
                        row -> codeTableKey(row.getReplaceGroup(), row.getSourceField(), row.getSourceValue()),
                        CodeTableDto::getTargetValue,
                        (left, right) -> right
                ));
    }

    private List<LiaReportOutputSettingDto> parseOutputSettings(Sheet sheet) {
        if (sheet == null || sheet.getRow(0) == null) {
            return List.of();
        }

        Map<String, Integer> headers = toHeaderMap(sheet.getRow(0));
        List<LiaReportOutputSettingDto> settings = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            addOutputSetting(settings, row, headers, "outputFileTxt", "txt");
            addOutputSetting(settings, row, headers, "outputFileZip", "zip");
            addOutputSetting(settings, row, headers, "outputFileExcel", "excel");
        }
        return settings;
    }

    private void addOutputSetting(
            List<LiaReportOutputSettingDto> settings,
            Row row,
            Map<String, Integer> headers,
            String enabledColumn,
            String outputType
    ) {
        if (isEnabled(value(row, headers, enabledColumn))) {
            settings.add(LiaReportOutputSettingDto.builder()
                    .outputFileName(value(row, headers, "outputFileName"))
                    .outputType(outputType)
                    .dataSelectType(outputDataSelectType(row, headers))
                    .zipPassword(value(row, headers, "zipPassword"))
                    .settingDesc(value(row, headers, "settingDesc"))
                    .build());
        }
    }

    private String outputDataSelectType(Row row, Map<String, Integer> headers) {
        String value = optionalValue(row, headers, "dataSelectType");
        if (!isBlank(value)) {
            return value;
        }
        return optionalValueByHeaderPrefix(row, headers, "choose");
    }

    private String outputReplaceGroup(Row row, Map<String, Integer> headers) {
        String value = optionalValue(row, headers, "replaceGroup");
        if (!isBlank(value)) {
            return value;
        }
        return optionalValue(row, headers, "relacepGroup");
    }

    private String toOutputTypes(List<LiaReportOutputSettingDto> outputSettings) {
        if (outputSettings.isEmpty()) {
            return "txt,excel";
        }
        String outputTypes = outputSettings.stream()
                .map(LiaReportOutputSettingDto::getOutputType)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        return outputTypes;
    }

    private String zipPassword(List<LiaReportOutputSettingDto> outputSettings) {
        return outputSettings.stream()
                .filter(setting -> "zip".equalsIgnoreCase(setting.getOutputType()))
                .map(LiaReportOutputSettingDto::getZipPassword)
                .filter(password -> !isBlank(password))
                .findFirst()
                .orElse("");
    }

    private boolean isEnabled(String value) {
        return "V".equalsIgnoreCase(value)
                || "Y".equalsIgnoreCase(value)
                || "YES".equalsIgnoreCase(value)
                || "TRUE".equalsIgnoreCase(value)
                || "1".equals(value);
    }

    private Map<String, Integer> toHeaderMap(Row headerRow) {
        Map<String, Integer> headers = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String headerName = dataFormatter.formatCellValue(headerRow.getCell(i)).trim();
            if (!headerName.isBlank()) {
                headers.put(headerName, i);
            }
        }
        return headers;
    }

    private String value(Row row, Map<String, Integer> headers, String name) {
        Integer index = headers.get(name);
        if (index == null) {
            throw new IllegalArgumentException("規格檔缺少欄位：" + name);
        }
        return dataFormatter.formatCellValue(row.getCell(index)).trim();
    }

    private String optionalValue(Row row, Map<String, Integer> headers, String name) {
        Integer index = headers.get(name);
        if (index == null) {
            return "";
        }
        return dataFormatter.formatCellValue(row.getCell(index)).trim();
    }

    private String valueByAliases(Row row, Map<String, Integer> headers, String... aliases) {
        for (String alias : aliases) {
            String value = optionalValue(row, headers, alias);
            if (!isBlank(value)) {
                return value;
            }
        }
        throw new IllegalArgumentException("規格檔缺少欄位：" + String.join("/", aliases));
    }

    private String optionalValueByHeaderPrefix(Row row, Map<String, Integer> headers, String headerPrefix) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(headerPrefix))
                .map(Map.Entry::getValue)
                .findFirst()
                .map(index -> dataFormatter.formatCellValue(row.getCell(index)).trim())
                .orElse("");
    }

    private Integer toInt(String value) {
        if (isBlank(value)) {
            return null;
        }
        return Integer.parseInt(stripTrailingDecimal(value));
    }

    private String stripTrailingDecimal(String value) {
        if (value != null && value.endsWith(".0")) {
            return value.substring(0, value.length() - 2);
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String codeTableKey(String replaceGroup, String sourceField, String sourceValue) {
        return normalizeKeyPart(replaceGroup) + "|" + normalizeKeyPart(sourceField) + "|" + normalizeKeyPart(sourceValue);
    }

    private String normalizeKeyPart(String value) {
        return value == null ? "" : value.trim();
    }
}
