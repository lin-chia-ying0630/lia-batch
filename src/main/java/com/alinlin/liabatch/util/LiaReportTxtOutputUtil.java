package com.alinlin.liabatch.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * LIA通報 TXT 輸出工具。
 * <p>
 * 集中處理固定長度 TXT 的 Big5 編碼、換行與檔案寫入，避免 Service 混入 TXT 細節。
 */
public class LiaReportTxtOutputUtil {

    private static final Charset TXT_CHARSET = Charset.forName("Big5");

    public byte[] toBytes(String line) {
        return (line + System.lineSeparator()).getBytes(TXT_CHARSET);
    }

    public void write(Path output, String line) {
        try {
            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }
            Files.write(output, toBytes(line));
        } catch (IOException e) {
            throw new IllegalStateException("輸出LIA通報TXT檔失敗：" + output, e);
        }
    }
}
