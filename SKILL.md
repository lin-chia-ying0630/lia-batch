# LIA Batch Lia Report Skill

## Purpose

Use this project to generate the lia fixed-length report TXT from an Excel spec file (`.xlsx` or `.xls`) and MySQL source tables.

## Architecture

Follow three layers:

- `controller`: batch entry orchestration only.
- `service`: business flow, spec validation, fixed-length formatting, writing file creation, and final output publish. Service dependencies use interface + `Impl` implementation naming.
- `repository`: data access only. Use MyBatis mapper interfaces and XML `select` statements against MySQL.

## Batch Flow

1. Run jar through Trinity or command line.
2. Read the Excel spec file configured by `lia.report.spec-file` (`.xlsx` or `.xls`).
3. Validate `startPos`, `endPos`, `length`, and continuous positions.
4. Read source data from MySQL through repository/MyBatis.
5. Build `LiaReportData` with `company`, `policy`, `customer`, `product`, and `payment`.
6. Resolve `sourceFile + sourceField` from the Excel spec against `LiaReportData`.
7. Format by `dataType` and `formatRule`; `dataType=X` means left align and pad spaces, `dataType=9` means right align and pad zeroes.
8. Write a `.writing` fixed-length TXT.
9. Publish the writing file as the final output.

## MyBatis Rules

- Mapper interfaces live in `src/main/java/com/alinlin/liabatch/mapper`.
- Mapper XML files live in `src/main/resources/mapper`.
- Keep SQL in XML files, not in service classes.
- `LiaReportSourceDataMapper.xml` uses MySQL tables:
  `lia_company`, `lia_policy`, `lia_customer`, `lia_product`, `lia_payment`.
- Runtime data access always uses MySQL/MyBatis.

## MySQL Setup

Schema and sample inserts are provided:

- `src/main/resources/db/mysql/schema.sql`
- `src/main/resources/db/mysql/insert-sample-data.sql`

Example:

```bash
mysql -uroot -p < src/main/resources/db/mysql/schema.sql
mysql -uroot -p < src/main/resources/db/mysql/insert-sample-data.sql
```

## Output Rules

- `--output=target/lia-report.txt` writes to the given file.
- `--output=target/out/` or `--output=target/out` writes to a generated name:
  `LIA_REPORT_{companyCode}_yyyyMMddHHmmss.txt`.
- The writing file is named `{finalName}.writing`; after writing succeeds it is published to the final `.txt`.
- Output is written in Big5 encoding.

## Verification

Use:

```bash
./mvnw -Dmaven.repo.local=.m2/repository test
./mvnw -Dmaven.repo.local=.m2/repository package -DskipTests
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/
```

Run the batch:

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/
```

To switch the spec file:

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --lia.report.spec-file=lia-report-spec.xls --output=target/out/
```
