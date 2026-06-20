package com.alinlin.liabatch.util;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;
import com.alinlin.liabatch.dto.LiaReportData;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分隔符號文字產生器。
 * <p>
 * 依欄位規格取值並用指定分隔符號組成一行 TXT，適用於非固定長度的分隔檔。
 */
public class DelimitedTextBuilder {

    private final LiaReportFieldValueResolver valueResolver;
    private final String delimiter;

    public DelimitedTextBuilder(String delimiter, Map<String, String> codeTable) {
        this.delimiter = delimiter;
        this.valueResolver = new LiaReportFieldValueResolver(codeTable);
    }
    /*
    .specs.stream()把 Excel 裡的欄位設定逐筆拿出來。
    .sorted(Comparator.comparing(LiaFieldSpecDto::getStartPos))依照 startPos 排序，確保輸出欄位順序跟 Excel 設定一致。
    .map(spec -> valueResolver.resolveFormattedValue(reportData, spec))
    依照每個欄位設定去 reportData 取值，例如：POLICY.policyNo CUSTOMER.idNoPRODUCT.productCode或固定值 fixedValue同時也會做：codeTable 轉換數字格式處理小數位處理
    .collect(Collectors.joining(delimiter))把所有欄位用分隔符號接起來。*/
    public String buildLine(LiaReportData reportData, List<LiaFieldSpecDto> specs) {
        return specs.stream()
                .sorted(Comparator.comparing(LiaFieldSpecDto::getStartPos))
                .map(spec -> valueResolver.resolveFormattedValue(reportData, spec))
                .collect(Collectors.joining(delimiter));
    }
}
