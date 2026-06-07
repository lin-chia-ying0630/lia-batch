# Output File Publish Rule

## Purpose

The batch writes the lia report to a `.writing` file first, then publishes it to the final TXT name after the write succeeds.

This prevents Trinity or downstream jobs from reading an incomplete output file.

## Writing File

When the final output path is:

```text
target/out/LIA_REPORT_01_20260607103000.txt
```

the writing file is:

```text
target/out/LIA_REPORT_01_20260607103000.txt.writing
```

Flow:

1. Write fixed-length content to `.writing`.
2. Close file successfully.
3. Publish `.writing` to final `.txt`.
4. Replace existing final file if the same name already exists.

## Final File Name

When `--output` is a directory, the system generates:

```text
LIA_REPORT_{companyCode}_{yyyyMMddHHmmss}.txt
```

Example:

```text
LIA_REPORT_01_20260607103000.txt
```

`companyCode` comes from `LiaReportData.company.companyCode`.

If company data or company code is missing, the system uses:

```text
LIA_REPORT_NA_{yyyyMMddHHmmss}.txt
```

## Output Argument Rules

### Directory Output

These are treated as directories:

```bash
--output=target/out/
--output=target/out
```

The batch generates the final file name automatically.

### Explicit File Output

This is treated as an explicit file path:

```bash
--output=target/out/custom-report.txt
```

The batch uses the given file name directly.

The writing file becomes:

```text
target/out/custom-report.txt.writing
```

## Commands

Mock mode:

```bash
java -jar target/LIA-batch-0.0.1-SNAPSHOT.jar --output=target/out/
```
