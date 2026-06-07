package com.alinlin.liabatch.mapper;

import com.alinlin.liabatch.dto.CompanyDto;
import com.alinlin.liabatch.dto.CustomerDto;
import com.alinlin.liabatch.dto.PaymentDto;
import com.alinlin.liabatch.dto.PolicyDto;
import com.alinlin.liabatch.dto.ProductDto;
import org.apache.ibatis.annotations.Mapper;

/**
 * LIA通報來源資料 MyBatis Mapper。
 * <p>
 * 方法對應 src/main/resources/mapper/LiaReportSourceDataMapper.xml 中的 select SQL，用於查詢 MySQL 來源資料。
 */
@Mapper
public interface LiaReportSourceDataMapper {
    CompanyDto selectCompany();

    PolicyDto selectPolicy();

    CustomerDto selectCustomer();

    ProductDto selectProduct();

    PaymentDto selectPayment();
}
