package com.alinlin.liabatch.service.impl;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;
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

/**
 * LIA通報 Excel 規格讀取器實作。
 * <p>
 * 使用 Apache POI 讀取 classpath 內的 .xlsx 或 .xls 規格檔，並轉成 LiaFieldSpecDto 清單。
 */
@Component
public class LiaReportExcelSpecReaderImpl implements LiaReportExcelSpecReader {

    private final DataFormatter dataFormatter = new DataFormatter();

    @Override
    public List<LiaFieldSpecDto> read(String classpathLocation) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(classpathLocation)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("找不到規格檔：" + classpathLocation);
            }

            try (Workbook workbook = WorkbookFactory.create(inputStream)) {
                return parseSheet(workbook.getSheetAt(0));
            }
        } catch (IOException e) {
            throw new IllegalStateException("讀取規格檔失敗：" + classpathLocation, e);
        }
    }

    private List<LiaFieldSpecDto> parseSheet(Sheet sheet) {
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
                    .sortNo(toInt(value(row, headers, "sortNo")))
                    .targetField(value(row, headers, "targetField"))
                    .targetDesc(value(row, headers, "targetDesc"))
                    .startPos(toInt(value(row, headers, "startPos")))
                    .endPos(toInt(value(row, headers, "endPos")))
                    .length(toInt(value(row, headers, "length")))
                    .dataType(value(row, headers, "dataType"))
                    .sourceFile(value(row, headers, "sourceFile"))
                    .sourceField(value(row, headers, "sourceField"))
                    .required(value(row, headers, "required"))
                    .formatRule(value(row, headers, "formatRule"))
                    .build());
        }
        return specs;
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
}
