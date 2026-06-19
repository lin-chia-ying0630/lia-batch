package com.alinlin.liabatch.util;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;
import com.alinlin.liabatch.dto.LiaReportData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * LIA通報 Excel 輸出工具。
 * <p>
 * 集中處理 Excel 活頁簿建立、中文表頭、資料列與欄寬調整。
 */
public class LiaReportExcelOutputUtil {

    private final LiaReportFieldValueResolver valueResolver;

    public LiaReportExcelOutputUtil() {
        this(Map.of());
    }

    public LiaReportExcelOutputUtil(Map<String, String> codeTable) {
        this.valueResolver = new LiaReportFieldValueResolver(codeTable);
    }

    public byte[] toBytes(LiaReportData reportData, List<LiaFieldSpecDto> specs) {
        return toBytes("LIA通報資料", reportData, specs);
    }

    public byte[] toBytes(String sheetName, LiaReportData reportData, List<LiaFieldSpecDto> specs) {
        return toBytes(sheetName, List.of(reportData), specs);
    }

    public byte[] toBytes(String sheetName, List<LiaReportData> reportDataList, List<LiaFieldSpecDto> specs) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writeWorkbook(workbook, sheetName, reportDataList, specs);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("建立LIA通報Excel內容失敗", e);
        }
    }

    public void write(Path excelPath, LiaReportData reportData, List<LiaFieldSpecDto> specs) {
        write(excelPath, List.of(reportData), specs);
    }

    public void write(Path excelPath, List<LiaReportData> reportDataList, List<LiaFieldSpecDto> specs) {
        try {
            if (excelPath.getParent() != null) {
                Files.createDirectories(excelPath.getParent());
            }
            try (Workbook workbook = new XSSFWorkbook();
                 OutputStream outputStream = Files.newOutputStream(excelPath)) {
                writeWorkbook(workbook, sheetName(excelPath), reportDataList, specs);
                workbook.write(outputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("輸出LIA通報Excel檔失敗：" + excelPath, e);
        }
    }

    private void writeWorkbook(Workbook workbook, String sheetName, LiaReportData reportData, List<LiaFieldSpecDto> specs) {
        writeWorkbook(workbook, sheetName, List.of(reportData), specs);
    }

    private void writeWorkbook(Workbook workbook, String sheetName, List<LiaReportData> reportDataList, List<LiaFieldSpecDto> specs) {
        List<LiaFieldSpecDto> sortedSpecs = sortedSpecs(specs);
        Sheet sheet = workbook.createSheet(safeSheetName(sheetName));
        CellStyle headerStyle = headerStyle(workbook);
        writeHeader(sheet, headerStyle, sortedSpecs);
        writeDataRows(sheet, reportDataList, sortedSpecs);
        resizeColumns(sheet, sortedSpecs.size());
    }

    private List<LiaFieldSpecDto> sortedSpecs(List<LiaFieldSpecDto> specs) {
        return specs.stream()
                .sorted(Comparator.comparing(LiaFieldSpecDto::getStartPos))
                .toList();
    }

    private void writeHeader(Sheet sheet, CellStyle headerStyle, List<LiaFieldSpecDto> specs) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < specs.size(); i++) {
            LiaFieldSpecDto spec = specs.get(i);
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headerName(spec));
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeData(Sheet sheet, LiaReportData reportData, List<LiaFieldSpecDto> specs) {
        writeData(sheet, 1, reportData, specs);
    }

    private void writeDataRows(Sheet sheet, List<LiaReportData> reportDataList, List<LiaFieldSpecDto> specs) {
        for (int rowIndex = 0; rowIndex < reportDataList.size(); rowIndex++) {
            writeData(sheet, rowIndex + 1, reportDataList.get(rowIndex), specs);
        }
    }

    private void writeData(Sheet sheet, int rowNumber, LiaReportData reportData, List<LiaFieldSpecDto> specs) {
        Row dataRow = sheet.createRow(rowNumber);
        for (int i = 0; i < specs.size(); i++) {
            dataRow.createCell(i).setCellValue(valueResolver.resolveFormattedValue(reportData, specs.get(i)));
        }
    }

    private String headerName(LiaFieldSpecDto spec) {
        if (spec.getTargetDesc() != null && !spec.getTargetDesc().isBlank()) {
            return spec.getTargetDesc();
        }
        return spec.getTargetField();
    }

    private CellStyle headerStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private void resizeColumns(Sheet sheet, int size) {
        for (int i = 0; i < size; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 512, 12000));
        }
    }

    private String sheetName(Path excelPath) {
        String fileName = excelPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private String safeSheetName(String sheetName) {
        String safeName = sheetName == null || sheetName.isBlank() ? "Sheet1" : sheetName.trim();
        safeName = safeName.replaceAll("[\\\\/?*\\[\\]:]", "_");
        return safeName.length() > 31 ? safeName.substring(0, 31) : safeName;
    }
}
