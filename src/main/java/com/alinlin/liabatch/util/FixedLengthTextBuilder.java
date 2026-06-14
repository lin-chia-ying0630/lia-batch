package com.alinlin.liabatch.util;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;
import com.alinlin.liabatch.dto.LiaReportData;

import java.util.Comparator;
import java.util.List;

/**
 * 固定長度文字產生器。
 * <p>
 * 依 LiaFieldSpecDto 指定的 sourceFile/sourceField 或 fixedValue 取值，套用格式與 dataType 補位後組成一行 TXT。
 */
public class FixedLengthTextBuilder {

    private final LiaReportFieldValueResolver valueResolver = new LiaReportFieldValueResolver();

    public String buildLine(LiaReportData reportData, List<LiaFieldSpecDto> specs) {
        int lineLength = specs.stream()
                .map(LiaFieldSpecDto::getEndPos)
                .max(Integer::compareTo)
                .orElse(0);
        char[] line = " ".repeat(lineLength).toCharArray();

        for (LiaFieldSpecDto spec : specs.stream()
                .sorted(Comparator.comparing(LiaFieldSpecDto::getStartPos))
                .toList()) {
            String fixedValue = padValue(valueResolver.resolveFormattedValue(reportData, spec), spec);

            int startIndex = spec.getStartPos() - 1;
            for (int i = 0; i < fixedValue.length(); i++) {
                line[startIndex + i] = fixedValue.charAt(i);
            }
        }

        return new String(line);
    }

    private String padValue(String value, LiaFieldSpecDto spec) {
        String v = value == null ? "" : value;
        int length = spec.getLength();
        if (v.length() > length) {
            return v.substring(0, length);
        }

        int padLength = length - v.length();
        if ("9".equalsIgnoreCase(spec.getDataType())) {
            return "0".repeat(padLength) + v;
        }
        if ("X".equalsIgnoreCase(spec.getDataType())) {
            return v + " ".repeat(padLength);
        }
        throw new IllegalArgumentException("不支援的 dataType：" + spec.getDataType() + "，欄位：" + spec.getTargetField());
    }
}
