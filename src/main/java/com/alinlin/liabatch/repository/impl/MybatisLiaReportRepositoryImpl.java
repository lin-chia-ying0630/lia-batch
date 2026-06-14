package com.alinlin.liabatch.repository.impl;

import com.alinlin.liabatch.dto.CustomerDto;
import com.alinlin.liabatch.dto.LiaReportData;
import com.alinlin.liabatch.dto.PolicyDto;
import com.alinlin.liabatch.dto.ProductDto;
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
        PolicyDto policy = sourceDataMapper.selectPolicy();
        if (policy == null) {
            throw new IllegalStateException("找不到可產檔保單資料：需 lia_policy.active_flag='Y'，且 lia_customer.customer_id=lia_policy.policy_id，並且 lia_product.product_code=lia_policy.product_code");
        }
        CustomerDto customer = sourceDataMapper.selectCustomerByPolicyId(policy.getPolicyId());
        if (customer == null) {
            throw new IllegalStateException("找不到保單對應客戶：lia_customer.customer_id 必須等於 lia_policy.policy_id，policy_id=" + policy.getPolicyId());
        }
        ProductDto product = sourceDataMapper.selectProductByProductCode(policy.getProductCode());
        if (product == null) {
            throw new IllegalStateException("找不到保單對應商品：lia_product.product_code 必須等於 lia_policy.product_code，product_code=" + policy.getProductCode());
        }
        return LiaReportData.builder()
                .policy(policy)
                .customer(customer)
                .product(product)
                .payment(sourceDataMapper.selectPayment())
                .build();
    }
}
