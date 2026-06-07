package com.alinlin.liabatch.util;

import com.alinlin.liabatch.dto.LiaFieldSpecDto;

import java.util.Comparator;
import java.util.List;

/**
 * LIA通報規格檢核工具。
 * <p>
 * 檢查 Excel 欄位規格是否有起訖長度錯誤、必要欄位缺漏，以及固定長度位置是否連續。
 */
public final class SpecValidator {

    private SpecValidator() {
    }

    public static void validate(List<LiaFieldSpecDto> specs) {
        if (specs == null || specs.isEmpty()) {
            throw new IllegalArgumentException("LIA通報欄位規格不可為空");
        }

        for (LiaFieldSpecDto spec : specs) {
            validateRequired(spec);
        }

        int expectedStartPos = 1;
        for (LiaFieldSpecDto spec : specs.stream()
                .sorted(Comparator.comparing(LiaFieldSpecDto::getStartPos))
                .toList()) {
            int expectedLength = spec.getEndPos() - spec.getStartPos() + 1;
            if (expectedLength != spec.getLength()) {
                throw new IllegalArgumentException(
                        "欄位長度設定錯誤：" + spec.getTargetField()
                                + "，startPos=" + spec.getStartPos()
                                + "，endPos=" + spec.getEndPos()
                                + "，length=" + spec.getLength()
                                + "，應為=" + expectedLength
                );
            }
            if (spec.getStartPos() != expectedStartPos) {
                throw new IllegalArgumentException(
                        "欄位位置不連續：" + spec.getTargetField()
                                + "，startPos=" + spec.getStartPos()
                                + "，應為=" + expectedStartPos
                );
            }
            expectedStartPos = spec.getEndPos() + 1;
        }
    }

    private static void validateRequired(LiaFieldSpecDto spec) {
        if (spec.getStartPos() == null || spec.getEndPos() == null || spec.getLength() == null) {
            throw new IllegalArgumentException("欄位起訖或長度不可為空：" + spec.getTargetField());
        }
        if (spec.getStartPos() <= 0 || spec.getEndPos() < spec.getStartPos() || spec.getLength() <= 0) {
            throw new IllegalArgumentException("欄位起訖或長度設定錯誤：" + spec.getTargetField());
        }
        if (isBlank(spec.getSourceFile()) || isBlank(spec.getSourceField())) {
            throw new IllegalArgumentException("來源設定不可為空：" + spec.getTargetField());
        }
        if (isBlank(spec.getDataType())) {
            throw new IllegalArgumentException("dataType 不可為空：" + spec.getTargetField());
        }
        if (!"X".equalsIgnoreCase(spec.getDataType()) && !"9".equalsIgnoreCase(spec.getDataType())) {
            throw new IllegalArgumentException("dataType 只支援 X 或 9：" + spec.getTargetField());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
