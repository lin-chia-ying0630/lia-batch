package com.alinlin.liabatch.repository.impl;

import com.alinlin.liabatch.mapper.LiaReportSourceDataMapper;
import com.alinlin.liabatch.repository.LiaReportLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MySQL/MyBatis 版LIA批次執行紀錄 Repository。
 * <p>
 * 負責將批次執行紀錄寫入 lia_log，產生日與時間由 Java 批次端統一產生。
 */
@Repository
public class MybatisLiaReportLogRepositoryImpl implements LiaReportLogRepository {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final LiaReportSourceDataMapper sourceDataMapper;

    @Autowired
    public MybatisLiaReportLogRepositoryImpl(LiaReportSourceDataMapper sourceDataMapper) {
        this.sourceDataMapper = sourceDataMapper;
    }

    @Override
    public void insertExecutionLog(String content) {
        LocalDateTime now = LocalDateTime.now();
        sourceDataMapper.insertLiaLog(
                now.format(DATE_FORMATTER),
                now.format(TIME_FORMATTER),
                content
        );
    }
}
