package com.alinlin.liabatch.dto;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LIA通報輸出格式列舉。
 * <p>
 * 支援由命令列傳入單選或複選輸出格式，例如 txt、excel、zip、txt,excel。
 */
public enum LiaReportOutputType {
    TXT,
    EXCEL,
    ZIP;

    public static Set<LiaReportOutputType> parse(String value) {
        if (value == null || value.isBlank()) {
            return EnumSet.of(TXT, EXCEL);
        }

        Set<LiaReportOutputType> outputTypes = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(type -> !type.isBlank())
                .map(type -> LiaReportOutputType.valueOf(type.toUpperCase(Locale.ROOT)))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(LiaReportOutputType.class)));

        if (outputTypes.isEmpty()) {
            throw new IllegalArgumentException("至少需指定一種輸出格式：txt、excel 或 zip");
        }
        return outputTypes;
    }
}
