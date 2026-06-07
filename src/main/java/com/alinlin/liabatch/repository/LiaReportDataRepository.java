package com.alinlin.liabatch.repository;

import com.alinlin.liabatch.dto.LiaReportData;

/**
 * LIA通報資料存取介面。
 * <p>
 * Service 層只依賴此介面取得 LiaReportData，實際來源由 MyBatis repository 提供。
 */
public interface LiaReportDataRepository {
    LiaReportData selectReportData();
}
