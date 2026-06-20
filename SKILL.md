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
6. 組成 `LiaReportData`，包含 `policy`、`customer`、`product`、`payment`；公司代號等固定內容由 `fixedValue` 提供。
7. 依 Excel 規格的 `sourceFile + sourceField` 從 `LiaReportData` 取值；若兩欄皆空白，改用 `fixedValue` 固定值。
8. 依 `dataType` 與 `decimalPlaces` 格式化資料；`dataType=X` 表示靠左補空白，`dataType=9` 表示靠右補 `0`，`decimalPlaces` 表示數字欄位的小數位數。
9. 若 `outputFileDetail.relacepGroup` 或 `replaceGroup` 有填值，依 `codeTable` 將來源值由 `source_value` 轉成 `target_value`。
10. 依 `outputSettings` 工作表決定輸出格式與檔名，可輸出 TXT、ZIP、Excel 或複選輸出。
11. 若輸出 TXT，先寫入 `.writing` 固定長度 TXT 暫存檔。
12. TXT 寫入成功後，將 `.writing` 發布為正式 TXT 檔。
13. 若輸出 Excel，產生 `.xlsx` 檢視檔，第一列表頭使用 Excel 規格中的中文欄位說明 `targetDesc`。

## MyBatis 規則

- Mapper 介面放在 `src/main/java/com/alinlin/liabatch/mapper`。
- Mapper XML 放在 `src/main/resources/mapper`。
- SQL 放在 XML，不放在 service class。
- `LiaReportSourceDataMapper.xml` 使用下列 MySQL 資料表：
  `lia_policy`、`lia_customer`、`lia_product_order`、`lia_product`、`lia_payment`、`lia_log`。
- 一張保單可有多筆 `lia_product_order`；程式以 `ProductOrderDto` 承接商品訂單資料，產檔目前只取 `product_order_no=1` 的商品代碼。
- `aaa` 檔案使用 `selectReportData()` 產生單筆通報資料。
- `bbb` 檔案使用 `selectProductOrderReportData()` 依同一張保單的多筆 `lia_product_order` 產生多筆資料列。
- 執行時資料來源固定使用 MySQL/MyBatis。
- 每次批次觸發後會新增一筆 `lia_log`，欄位包含 `generate_date`、`generate_time` 與 `content`；成功會記錄輸出檔，失敗會記錄錯誤內容。

## MySQL 建檔

專案已提供 schema 與測試資料：

- `src/main/resources/db/mysql/schema.sql`
- `src/main/resources/db/mysql/insert-sample-data.sql`
- `src/main/resources/db/mysql/alter-payment-policy-id.sql`
- `src/main/resources/db/mysql/alter-product-order.sql`
- `src/main/resources/db/mysql/alter-lia-log.sql`

執行範例：

```bash
mysql -uroot -p < src/main/resources/db/mysql/schema.sql
mysql -uroot -p < src/main/resources/db/mysql/insert-sample-data.sql
```

## outputSettings 規則

`outputFileDetail` 工作表第一欄也有 `outputFileName`，用來對應 `outputSettings.outputFileName`。
例如 `outputSettings.outputFileName=aaa`，就會取 `outputFileDetail.outputFileName=aaa` 的欄位規格產生 `aaa.txt`、`aaa.xlsx`；若 `outputFileZip=report`，則會把該列的 TXT/Excel 項目放入 `report.zip`。

`outputFileDetail` 的來源欄位規則：

- `sourceFile` 與 `sourceField` 兩欄都填：從對應 DTO 取值。
- `sourceFile` 與 `sourceField` 兩欄都空白：使用 `fixedValue`。
- `sourceFile` 與 `sourceField` 只能同時填或同時空白，不可只填其中一欄。
- `fixedValue` 可空白；空白時仍會依 `dataType` 做補位。
- 公司代號目前不查 DB，請在 `COMPANY_CODE` 欄位使用 `fixedValue=109`。

## codeTable 規則

`codeTable` 工作表用於代碼轉換，欄位如下：

- `replaceGroup`：轉換群組，對應 `outputFileDetail` 的 `relacepGroup` 或 `replaceGroup`。
- `sourceField`：來源欄位名稱，對應 `outputFileDetail.sourceField`。
- `source_value`：來源資料查出的原始值。
- `target_value`：實際輸出的轉換值。
- `codeDesc`：轉換說明。

當 `outputFileDetail` 某列有填 `relacepGroup` 或 `replaceGroup` 時，程式會用
`replaceGroup + sourceField + source_value` 到 `codeTable` 找 `target_value`。
找不到時不轉換，直接使用原始來源值輸出。

`lia-report-spec.xlsx` 的 `outputSettings` 工作表以一列代表一組輸出檔：

- 第一欄 `outputFileName`：產生的檔名基底，例如 `aaa`，系統會依格式產生 `aaa.txt`、`aaa.xlsx`；ZIP 檔名由 `outputFileZip` 決定。
- `choose`：填 `1` 表示使用 `selectReportData()` 產生單筆資料；填 `2` 表示使用 `selectProductOrderReportData()` 依商品訂單產生多筆資料。
- `txtDelimiter`：TXT 分隔符號；空白表示固定長度 TXT，填 `|`、`,`、`\t` 或 `TAB` 表示產生分隔檔。
- `outputFileTxt`：填 `V` 表示產生 TXT。
- `outputFileExcel`：填 `V` 表示產生 Excel。
- `outputFileZip`：填 ZIP 檔名表示產生 ZIP；多列填相同 ZIP 檔名時，會合併打包到同一個 ZIP。為相容舊設定，填 `V` 仍代表用 `outputFileName` 作為 ZIP 檔名。
- `zipPassword`：只給 `zip` 使用，請填已經 MD5 處理過的密碼；空白表示只壓縮不加密。
- `settingDesc`：設定說明。

## 輸出規則

- `--output=target/out/` 或 `--output=target/out` 會視為輸出目錄，檔名使用 `outputSettings.outputFileName` 加上副檔名。
- `--output=target/lia-report.txt` 會視為指定輸出位置，目錄使用 `target/`，檔名仍使用 `outputSettings.outputFileName` 加上副檔名。
- 命令列 `--output-types=` 仍可臨時覆蓋哪些格式要輸出，但檔名仍以 `outputSettings.outputFileName` 為準。
- 命令列 `--zip-password=` 仍可臨時覆蓋 Excel 的 `zipPassword`。
- 同一個 ZIP 檔名若多列都有 `zipPassword`，密碼必須相同；若只有其中一列有填，會使用該密碼。
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

正式排程建議將檔名基底、是否產生、ZIP 檔名與 `zipPassword` 都設在 `lia-report-spec.xlsx` 的 `outputSettings` 工作表，命令列只保留 `--output`。

## 切換規格檔

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --lia.report.spec-file=lia-report-spec.xls --output=target/out/
```
