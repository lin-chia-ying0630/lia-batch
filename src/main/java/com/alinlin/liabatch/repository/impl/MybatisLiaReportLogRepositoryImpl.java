package com.alinlin.liabatch.repository.impl;

import com.alinlin.liabatch.mapper.LiaReportSourceDataMapper;
import com.alinlin.liabatch.repository.LiaReportLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * MySQL/MyBatis 版 LIA 通報執行紀錄 Repository。
 * <p>
 * 將每次批次觸發的產生日、時間與執行內容寫入 lia_log。
 */
@Repository
public class MybatisLiaReportLogRepositoryImpl implements LiaReportLogRepository {

    private final LiaReportSourceDataMapper sourceDataMapper;

    @Autowired
    public MybatisLiaReportLogRepositoryImpl(LiaReportSourceDataMapper sourceDataMapper) {
        this.sourceDataMapper = sourceDataMapper;
    }

    @Override
    public void insertLog(LocalDate generateDate, LocalTime generateTime, String content) {
        sourceDataMapper.insertLiaLog(generateDate, generateTime, content);
    }
}
