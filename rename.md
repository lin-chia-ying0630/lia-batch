# 輸出檔案發布規則

## 目的

批次產生 LIA 通報檔時，會先寫入 `.writing` 暫存檔，確認寫入成功後，才發布為正式 TXT 檔。
`lia-report-spec.xlsx` 的 `outputSettings` 工作表會決定要產生哪些檔案，以及每一種檔案的檔名。
第一欄 `outputFileName` 是檔名基底，例如 `aaa`，系統會依輸出格式產生 `aaa.txt`、`aaa.xlsx`；`outputFileZip` 則填入要打包的 ZIP 檔名。
`choose` 欄位填 `1` 代表使用 `selectReportData()`，填 `2` 代表使用 `selectProductOrderReportData()`。
`txtDelimiter` 欄位可設定 TXT 分隔符號；空白表示固定長度 TXT，填 `|`、`,`、`\t` 或 `TAB` 表示產生分隔檔。
`outputFileTxt`、`outputFileExcel` 欄位填 `V` 才會產生對應檔案；`outputFileZip` 欄位填 ZIP 檔名才會產生 ZIP。
多列 `outputFileZip` 填同一個 ZIP 檔名時，會把多個輸出檔合併打包到同一個 ZIP。
第一張工作表 `outputFileDetail` 也有 `outputFileName`，用來對應 `outputSettings`，因此可以設定多組檔案與各自欄位。
`outputFileDetail` 的 `sourceFile` 與 `sourceField` 可以同時空白；若同時空白，系統會使用 `fixedValue` 固定值欄位。
`outputFileDetail` 若填 `relacepGroup` 或 `replaceGroup`，系統會到 `codeTable` 工作表依 `replaceGroup + sourceField + source_value` 找出 `target_value` 後輸出；找不到對應資料時不轉換，直接輸出原始值。
console 後端每次批次觸發結束後會新增一筆 `lia_log`，保存產生日、產生時間與執行內容；`content` 內容與畫面「執行紀錄」顯示的 log 一致。

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
3. 判斷 `outputFileTxt`、`outputFileExcel` 是否為 `V`，並判斷 `outputFileZip` 是否有填 ZIP 檔名。
4. 若包含 `txt`，將固定長度內容寫入 `.writing`。
5. 正常關閉 TXT 暫存檔。
6. 將 `.writing` 發布為正式 `.txt`。
7. 如果正式 TXT 已存在，使用本次產生的檔案覆蓋。
8. 若包含 `excel`，在正式檔名基底旁邊產生 Excel 檢視檔。
9. 若 `outputFileZip` 有填值，依該欄位的 ZIP 檔名分組打包；多列同名會產生同一個 ZIP。
10. 若 zip 列的 `zipPassword` 有填值，ZIP 使用該 MD5 字串作為密碼；同一 ZIP 檔名不可設定不同密碼。

## 正式檔名

若 `outputSettings` 第一欄設定為：

```text
aaa
```

且 `outputFileTxt`、`outputFileExcel` 填 `V`、`outputFileZip` 填 `aaa`，`--output=target/out/` 實際產生：

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
outputFileName | choose | outputFileTxt | outputFileExcel | outputFileZip | zipPassword | settingDesc
aaa            | 1      | V             | V               | aaa           |             | 固定長度TXT通報檔
bbb            | 2      | V             | V               | aaa           |             | 商品明細通報檔
```

`outputFileTxt`、`outputFileExcel` 填 `V` 表示產生，空白表示不產生。
`outputFileZip` 填 ZIP 檔名表示產生 ZIP，空白表示不打包；上例會把 `aaa.txt`、`aaa.xlsx`、`bbb.txt`、`bbb.xlsx` 放進同一個 `aaa.zip`。

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
