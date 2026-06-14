# LIA 批次通報產檔說明

## 目的

本專案用來依據 Excel 規格檔（`.xlsx` 或 `.xls`）與 MySQL 來源資料，產生 LIA 固定長度 TXT 通報檔，並同步產生一份方便人工檢視的 Excel 檔。

## 架構

專案採三層架構：

- `controller`：只負責批次入口與命令列參數解析。
- `service`：負責業務流程、規格檢核、輸出格式判斷、`.writing` 暫存檔建立與正式檔發布。Service 依賴採用介面加 `Impl` 實作命名。
- `repository`：只負責資料存取。透過 MyBatis mapper 介面與 XML `select` SQL 查詢 MySQL。
- `util`：負責可重用的純處理邏輯，例如 TXT、Excel、ZIP 產檔細節。

## 產檔工具分工

- `LiaReportTxtOutputUtil`：負責 TXT Big5 編碼、換行與檔案寫入。
- `LiaReportExcelOutputUtil`：負責 Excel 活頁簿建立、中文表頭、資料列與欄寬調整。
- `LiaReportZipOutputUtil`：負責 ZIP 壓縮、密碼加密與項目寫入。

## 批次流程

1. 透過 Trinity 或命令列執行 jar。
2. 讀取 `lia.report.spec-file` 設定的 Excel 規格檔（`.xlsx` 或 `.xls`）。
3. 讀取 `outputFileDetail` 工作表，依 `outputFileName` 分組欄位規格。
4. 檢查每一個 `outputFileName` 群組內的 `startPos`、`endPos`、`length` 與欄位位置是否連續正確。
5. 透過 repository/MyBatis 從 MySQL 讀取來源資料。
6. 組成 `LiaReportData`，包含 `company`、`policy`、`customer`、`product`、`payment`。
7. 依 Excel 規格的 `sourceFile + sourceField` 從 `LiaReportData` 取值；若兩欄皆空白，改用 `fixedValue` 固定值。
8. 依 `dataType` 與 `decimalPlaces` 格式化資料；`dataType=X` 表示靠左補空白，`dataType=9` 表示靠右補 `0`，`decimalPlaces` 表示數字欄位的小數位數。
9. 依 `outputSettings` 工作表決定輸出格式與檔名，可輸出 TXT、ZIP、Excel 或複選輸出。
10. 若輸出 TXT，先寫入 `.writing` 固定長度 TXT 暫存檔。
11. TXT 寫入成功後，將 `.writing` 發布為正式 TXT 檔。
12. 若輸出 Excel，產生 `.xlsx` 檢視檔，第一列表頭使用 Excel 規格中的中文欄位說明 `targetDesc`。

## MyBatis 規則

- Mapper 介面放在 `src/main/java/com/alinlin/liabatch/mapper`。
- Mapper XML 放在 `src/main/resources/mapper`。
- SQL 放在 XML，不放在 service class。
- `LiaReportSourceDataMapper.xml` 使用下列 MySQL 資料表：
  `lia_company`、`lia_policy`、`lia_customer`、`lia_product`、`lia_payment`。
- 執行時資料來源固定使用 MySQL/MyBatis。

## MySQL 建檔

專案已提供 schema 與測試資料：

- `src/main/resources/db/mysql/schema.sql`
- `src/main/resources/db/mysql/insert-sample-data.sql`

執行範例：

```bash
mysql -uroot -p < src/main/resources/db/mysql/schema.sql
mysql -uroot -p < src/main/resources/db/mysql/insert-sample-data.sql
```

## outputSettings 規則

`outputFileDetail` 工作表第一欄也有 `outputFileName`，用來對應 `outputSettings.outputFileName`。
例如 `outputSettings.outputFileName=aaa`，就會取 `outputFileDetail.outputFileName=aaa` 的欄位規格產生 `aaa.txt`、`aaa.xlsx` 與 `aaa.zip`。

`outputFileDetail` 的來源欄位規則：

- `sourceFile` 與 `sourceField` 兩欄都填：從對應 DTO 取值。
- `sourceFile` 與 `sourceField` 兩欄都空白：使用 `fixedValue`。
- `sourceFile` 與 `sourceField` 只能同時填或同時空白，不可只填其中一欄。
- `fixedValue` 可空白；空白時仍會依 `dataType` 做補位。

`lia-report-spec.xlsx` 的 `outputSettings` 工作表以一列代表一組輸出檔：

- 第一欄 `outputFileName`：產生的檔名基底，例如 `aaa`，系統會依格式產生 `aaa.txt`、`aaa.zip`、`aaa.xlsx`。
- `outputFileTxt`：填 `V` 表示產生 TXT。
- `outputFileExcel`：填 `V` 表示產生 Excel。
- `outputFileZip`：填 `V` 表示產生 ZIP。
- `zipPassword`：只給 `zip` 使用，請填已經 MD5 處理過的密碼；空白表示只壓縮不加密。
- `settingDesc`：設定說明。

## 輸出規則

- `--output=target/out/` 或 `--output=target/out` 會視為輸出目錄，檔名使用 `outputSettings.outputFileName` 加上副檔名。
- `--output=target/lia-report.txt` 會視為指定輸出位置，目錄使用 `target/`，檔名仍使用 `outputSettings.outputFileName` 加上副檔名。
- 命令列 `--output-types=` 仍可臨時覆蓋哪些格式要輸出，但檔名仍以 `outputSettings.outputFileName` 為準。
- 命令列 `--zip-password=` 仍可臨時覆蓋 Excel 的 `zipPassword`。
- Excel 檔內的 sheet 名稱與檔名基底相同，例如 `aaa.xlsx` 的 sheet 名稱為 `aaa`。
- `.writing` 暫存檔命名為 `{finalName}.writing`，寫入成功後才發布成正式 `.txt`。
- TXT 使用 Big5 編碼輸出。

## 驗證

使用下列指令測試與封裝：

```bash
./mvnw -Dmaven.repo.local=.m2/repository test
./mvnw -Dmaven.repo.local=.m2/repository package -DskipTests
```

## 執行批次

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/
```

只產生 TXT：

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/ --output-types=txt
```

只產生 Excel：

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/ --output-types=excel
```

只產生 ZIP：

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/ --output-types=zip
```

產生加密 ZIP：

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/ --output-types=zip --zip-password=098f6bcd4621d373cade4e832627b4f6
```

正式排程建議將檔名基底、是否產生與 `zipPassword` 都設在 `lia-report-spec.xlsx` 的 `outputSettings` 工作表，命令列只保留 `--output`。

## 切換規格檔

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --lia.report.spec-file=lia-report-spec.xls --output=target/out/
```
