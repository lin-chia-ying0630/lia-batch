package com.alinlin.liabatch.util;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;
import com.alinlin.liabatch.dto.LiaReportData;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * LIA通報欄位取值工具。
 * <p>
 * 依 Excel 規格的 sourceFile/sourceField 從 LiaReportData 找到來源欄位；若來源欄位空白，改用 fixedValue。
 */
public class LiaReportFieldValueResolver {

    public String resolveFormattedValue(LiaReportData reportData, LiaFieldSpecDto spec) {
        if (isBlank(spec.getSourceFile()) && isBlank(spec.getSourceField())) {
            return formatValue(spec.getFixedValue(), spec);
        }

        Object sourceObject = reportData.sourceObject(spec.getSourceFile());
        if (sourceObject == null) {
            throw new IllegalArgumentException("找不到來源檔案/來源物件：" + spec.getSourceFile());
        }
        Object rawValue = getFieldValue(sourceObject, spec.getSourceField());
        return formatValue(rawValue, spec);
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
        if ("9".equalsIgnoreCase(spec.getDataType())) {
            return formatNumericValue(rawValue, spec);
        }
        return rawValue.toString().trim();
    }

    private String formatNumericValue(Object rawValue, LiaFieldSpecDto spec) {
        Integer decimalPlaces = spec.getDecimalPlaces();
        if (decimalPlaces != null) {
            BigDecimal number = rawValue instanceof BigDecimal value
                    ? value
                    : new BigDecimal(rawValue.toString().replace(",", ""));
            BigDecimal shiftedNumber = number.setScale(decimalPlaces, RoundingMode.HALF_UP)
                    .movePointRight(decimalPlaces);
            return shiftedNumber.setScale(0, RoundingMode.UNNECESSARY).toPlainString();
        }
        return rawValue.toString().replaceAll("[^0-9]", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
