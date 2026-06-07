package com.alinlin.liabatch.service;

import java.nio.file.Path;

/**
 * LIA通報主要 Service 介面。
 * <p>
 * 定義產生LIA固定長度通報檔的對外入口，Controller 只依賴此介面。
 */
public interface LiaReportService {
    Path generate(String outputArg);
}
