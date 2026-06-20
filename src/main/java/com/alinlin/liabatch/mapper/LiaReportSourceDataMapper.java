package com.alinlin.liabatch.mapper;

import com.alinlin.liabatch.entity.CustomerDto;
import com.alinlin.liabatch.entity.PaymentDto;
import com.alinlin.liabatch.entity.PolicyDto;
import com.alinlin.liabatch.entity.ProductOrderDto;
import com.alinlin.liabatch.entity.ProductDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * LIA通報來源資料 MyBatis Mapper。
 * <p>
 * 方法對應 src/main/resources/mapper/LiaReportSourceDataMapper.xml 中的 select SQL，用於查詢 MySQL 來源資料。
 */
@Mapper
public interface LiaReportSourceDataMapper {
    PolicyDto selectPolicy();

    CustomerDto selectCustomerByPolicyId(@Param("policyId") Long policyId);

    ProductOrderDto selectFirstProductOrder(@Param("policyNo") String policyNo, @Param("policySeq") String policySeq);

    List<ProductOrderDto> selectProductOrders(@Param("policyNo") String policyNo, @Param("policySeq") String policySeq);

    ProductDto selectProductByProductCode(@Param("productCode") String productCode);

    PaymentDto selectPaymentByPolicyId(@Param("policyId") Long policyId);

    void insertLiaLog(
            @Param("generateDate") LocalDate generateDate,
            @Param("generateTime") LocalTime generateTime,
            @Param("content") String content
    );
}
