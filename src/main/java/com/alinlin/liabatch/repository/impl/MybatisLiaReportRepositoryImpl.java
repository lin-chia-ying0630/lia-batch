package com.alinlin.liabatch.repository.impl;

import com.alinlin.liabatch.dto.LiaReportData;
import com.alinlin.liabatch.mapper.LiaReportSourceDataMapper;
import com.alinlin.liabatch.repository.LiaReportDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * MySQL/MyBatis 版LIA通報資料 Repository。
 * <p>
 * 透過 LiaReportSourceDataMapper 執行 XML select 並組成 LiaReportData。
 */
@Repository
public class MybatisLiaReportRepositoryImpl implements LiaReportDataRepository {

    private final LiaReportSourceDataMapper sourceDataMapper;

    @Autowired
    public MybatisLiaReportRepositoryImpl(LiaReportSourceDataMapper sourceDataMapper) {
        this.sourceDataMapper = sourceDataMapper;
    }

    @Override
    public LiaReportData selectReportData() {
        return LiaReportData.builder()
                .company(sourceDataMapper.selectCompany())
                .policy(sourceDataMapper.selectPolicy())
                .customer(sourceDataMapper.selectCustomer())
                .product(sourceDataMapper.selectProduct())
                .payment(sourceDataMapper.selectPayment())
                .build();
    }
}
