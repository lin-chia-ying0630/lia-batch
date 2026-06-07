package com.alinlin.liabatch.util;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;
import com.alinlin.liabatch.dto.LiaReportData;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * 固定長度文字產生器。
 * <p>
 * 依 LiaFieldSpecDto 指定的 sourceFile/sourceField 從 LiaReportData 取值，套用格式與 dataType 補位後組成一行 TXT。
 */
public class FixedLengthTextBuilder {

    public String buildLine(LiaReportData reportData, List<LiaFieldSpecDto> specs) {
        int lineLength = specs.stream()
                .map(LiaFieldSpecDto::getEndPos)
                .max(Integer::compareTo)
                .orElse(0);
        char[] line = " ".repeat(lineLength).toCharArray();

        for (LiaFieldSpecDto spec : specs.stream()
                .sorted(Comparator.comparing(LiaFieldSpecDto::getStartPos))
                .toList()) {
            Object sourceObject = reportData.sourceObject(spec.getSourceFile());
            if (sourceObject == null) {
                throw new IllegalArgumentException("找不到來源檔案/來源物件：" + spec.getSourceFile());
            }

            Object rawValue = getFieldValue(sourceObject, spec.getSourceField());
            String fixedValue = padValue(formatValue(rawValue, spec), spec);

            int startIndex = spec.getStartPos() - 1;
            for (int i = 0; i < fixedValue.length(); i++) {
                line[startIndex + i] = fixedValue.charAt(i);
            }
        }

        return new String(line);
    }

    private Object getFieldValue(Object sourceObject, String fieldName) {
        try {
            Field field = sourceObject.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(sourceObject);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "來源物件 " + sourceObject.getClass().getSimpleName() + " 找不到欄位：" + fieldName,
                    e
            );
        }
    }

    private String formatValue(Object rawValue, LiaFieldSpecDto spec) {
        if (rawValue == null) {
            return "";
        }
        if ("AMOUNT".equalsIgnoreCase(spec.getFormatRule())) {
            if (rawValue instanceof BigDecimal amount) {
                return amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
            }
            return new BigDecimal(rawValue.toString()).setScale(0, RoundingMode.HALF_UP).toPlainString();
        }
        if ("NUMBER".equalsIgnoreCase(spec.getFormatRule()) || "9".equalsIgnoreCase(spec.getDataType())) {
            return rawValue.toString().replaceAll("[^0-9]", "");
        }
        return rawValue.toString().trim();
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
