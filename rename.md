# 輸出檔案發布規則

## 目的

批次產生 LIA 通報檔時，會先寫入 `.writing` 暫存檔，確認寫入成功後，才發布為正式 TXT 檔。
`lia-report-spec.xlsx` 的 `outputSettings` 工作表會決定要產生哪些檔案，以及每一種檔案的檔名。
第一欄 `outputFileName` 是檔名基底，例如 `aaa`，系統會依輸出格式產生 `aaa.txt`、`aaa.zip`、`aaa.xlsx`。
`outputFileTxt`、`outputFileExcel`、`outputFileZip` 欄位填 `V` 才會產生對應檔案。
第一張工作表 `outputFileDetail` 也有 `outputFileName`，用來對應 `outputSettings`，因此可以設定多組檔案與各自欄位。
`outputFileDetail` 的 `sourceFile` 與 `sourceField` 可以同時空白；若同時空白，系統會使用 `fixedValue` 固定值欄位。

這樣可以避免 Trinity 或後續作業讀到尚未寫完的不完整檔案。

## 寫入中檔案

當正式輸出路徑為：

```text
target/out/LIA_REPORT_01_20260607103000.txt
```

寫入中檔案為：

```text
target/out/LIA_REPORT_01_20260607103000.txt.writing
```

流程：

1. 讀取 `lia-report-spec.xlsx` 的 `outputSettings`。
2. 依 `outputSettings.outputFileName` 找到 `outputFileDetail.outputFileName` 相同的欄位規格。
3. 判斷 `outputFileTxt`、`outputFileExcel`、`outputFileZip` 是否為 `V`。
4. 若包含 `txt`，將固定長度內容寫入 `.writing`。
5. 正常關閉 TXT 暫存檔。
6. 將 `.writing` 發布為正式 `.txt`。
7. 如果正式 TXT 已存在，使用本次產生的檔案覆蓋。
8. 若包含 `excel`，在正式檔名基底旁邊產生 Excel 檢視檔。
9. 若包含 `zip`，在正式檔名基底旁邊產生 ZIP 檔。
10. 若 zip 列的 `zipPassword` 有填值，ZIP 使用該 MD5 字串作為密碼。

## 正式檔名

當 `--output` 是目錄時，系統會自動產生檔名：

```text
LIA_REPORT_{companyCode}_{yyyyMMddHHmmss}.txt
```

若 `outputSettings` 第一欄設定為：

```text
aaa
```

且 `outputFileTxt`、`outputFileExcel`、`outputFileZip` 都填 `V`，`--output=target/out/` 實際產生：

```text
target/out/aaa.txt
target/out/aaa.zip
target/out/aaa.xlsx
```

`aaa.xlsx` 內的 sheet 名稱也會是 `aaa`。

## 輸出參數規則

### 輸出格式

`lia-report-spec.xlsx` 的 `outputSettings` 可設定：

```text
outputFileName | outputFileTxt | outputFileExcel | outputFileZip | zipPassword | settingDesc
aaa            | V             | V               | V             |             | 固定長度TXT通報檔；填V才產生
```

`outputFileTxt`、`outputFileExcel`、`outputFileZip` 填 `V` 表示產生，空白表示不產生。

### ZIP 密碼

`lia-report-spec.xlsx` 的 zip 列 `zipPassword` 用於設定 ZIP 密碼：

```bash
098f6bcd4621d373cade4e832627b4f6
```

注意：此欄位需填入已經 MD5 處理過的字串，批次程式不會再做 MD5。
未填 `zipPassword` 時，ZIP 只壓縮不加密。
命令列 `--output-types` 與 `--zip-password` 只作為臨時覆蓋 Excel 設定使用。

### 目錄輸出

下列參數會被視為目錄：

```bash
--output=target/out/
--output=target/out
```

批次會自動產生正式檔名。

### 指定檔案輸出

下列參數會被視為指定檔案路徑：

```bash
--output=target/out/custom-report.txt
```

批次會直接使用指定的檔名。

寫入中檔案會是：

```text
target/out/custom-report.txt.writing
```

Excel 檢視檔會是：

```text
target/out/custom-report.xlsx
```

ZIP 檔會是：

```text
target/out/custom-report.zip
```

## 執行指令

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/ --output-types=txt
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/ --output-types=excel
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/ --output-types=zip
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/ --output-types=txt,excel
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/ --output-types=txt,excel,zip
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/ --output-types=zip --zip-password=098f6bcd4621d373cade4e832627b4f6
```
