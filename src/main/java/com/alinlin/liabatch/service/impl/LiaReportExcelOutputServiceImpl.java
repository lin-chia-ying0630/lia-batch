package com.alinlin.liabatch.service.impl;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;
import com.alinlin.liabatch.dto.LiaReportData;
import com.alinlin.liabatch.service.LiaReportExcelOutputService;
import com.alinlin.liabatch.util.LiaReportExcelOutputUtil;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
 * LIA通報 Excel 輸出 Service 實作。
 * <p>
 * 依固定長度規格排序欄位，第一列輸出中文表頭，第二列輸出 MySQL 來源資料轉換後的欄位值。
 */
@Service
public class LiaReportExcelOutputServiceImpl implements LiaReportExcelOutputService {

    private final LiaReportExcelOutputUtil excelOutputUtil = new LiaReportExcelOutputUtil();

    @Override
    public Path write(Path txtOutputPath, LiaReportData reportData, List<LiaFieldSpecDto> specs) {
        Path excelPath = toExcelPath(txtOutputPath);
        excelOutputUtil.write(excelPath, reportData, specs);
        return excelPath;
    }

    private Path toExcelPath(Path txtOutputPath) {
        String fileName = txtOutputPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String excelName = dotIndex > 0 ? fileName.substring(0, dotIndex) + ".xlsx" : fileName + ".xlsx";
        return txtOutputPath.resolveSibling(excelName);
    }
}
