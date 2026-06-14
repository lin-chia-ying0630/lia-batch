package com.alinlin.liabatch.util;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * LIA通報 ZIP 輸出工具。
 * <p>
 * 集中處理 ZIP 壓縮、加密與檔案項目寫入；密碼若有填入，會直接使用呼叫端提供的 MD5 字串。
 */
public class LiaReportZipOutputUtil {

    public void write(Path zipPath, Map<String, byte[]> entries) {
        write(zipPath, entries, "");
    }

    public void write(Path zipPath, Map<String, byte[]> entries, String password) {
        try {
            if (zipPath.getParent() != null) {
                Files.createDirectories(zipPath.getParent());
            }
            Files.deleteIfExists(zipPath);

            ZipFile zipFile = hasPassword(password)
                    ? new ZipFile(zipPath.toFile(), password.toCharArray())
                    : new ZipFile(zipPath.toFile());

            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zipFile.addStream(new ByteArrayInputStream(entry.getValue()), zipParameters(entry.getKey(), password));
            }
        } catch (IOException e) {
            throw new IllegalStateException("輸出LIA通報ZIP檔失敗：" + zipPath, e);
        }
    }

    private ZipParameters zipParameters(String fileName, String password) {
        ZipParameters parameters = new ZipParameters();
        parameters.setFileNameInZip(fileName);
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        parameters.setCompressionLevel(CompressionLevel.NORMAL);
        if (hasPassword(password)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES);
            parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        }
        return parameters;
    }

    private boolean hasPassword(String password) {
        return password != null && !password.isBlank();
    }
}
