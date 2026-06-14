package com.alinlin.liabatch.repository.impl;

import com.alinlin.liabatch.entity.CustomerDto;
import com.alinlin.liabatch.dto.LiaReportData;
import com.alinlin.liabatch.entity.PaymentDto;
import com.alinlin.liabatch.entity.PolicyDto;
import com.alinlin.liabatch.entity.ProductOrderDto;
import com.alinlin.liabatch.entity.ProductDto;
import com.alinlin.liabatch.mapper.LiaReportSourceDataMapper;
import com.alinlin.liabatch.repository.LiaReportDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

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
            throw new IllegalStateException("找不到可產檔保單資料：需 lia_policy.active_flag='Y'，且 lia_customer.customer_id=lia_policy.policy_id，並且 lia_product_order.product_order_no=1 可串到 lia_product.product_code");
        }
        CustomerDto customer = sourceDataMapper.selectCustomerByPolicyId(policy.getPolicyId());
        if (customer == null) {
            throw new IllegalStateException("找不到保單對應客戶：lia_customer.customer_id 必須等於 lia_policy.policy_id，policy_id=" + policy.getPolicyId());
        }
        ProductOrderDto productOrder = sourceDataMapper.selectFirstProductOrder(policy.getPolicyNo(), policy.getPolicySeq());
        if (productOrder == null) {
            throw new IllegalStateException("找不到保單第一順位商品訂單：lia_product_order.policy_no/policy_seq 必須等於保單，且 product_order_no=1，policy_no=" + policy.getPolicyNo() + ", policy_seq=" + policy.getPolicySeq());
        }
        ProductDto product = sourceDataMapper.selectProductByProductCode(productOrder.getProductCode());
        if (product == null) {
            throw new IllegalStateException("找不到保單第一順位商品：lia_product.product_code 必須等於 lia_product_order.product_code，product_code=" + productOrder.getProductCode());
        }
        PaymentDto payment = sourceDataMapper.selectPaymentByPolicyId(policy.getPolicyId());
        if (payment == null) {
            throw new IllegalStateException("找不到保單對應繳費資料：lia_payment.policy_id 必須等於 lia_policy.policy_id，policy_id=" + policy.getPolicyId());
        }
        return LiaReportData.builder()
                .policy(policy)
                .customer(customer)
                .productOrder(productOrder)
                .product(product)
                .payment(payment)
                .build();
    }

    @Override
    public List<LiaReportData> selectProductOrderReportData() {
        LiaReportData baseReportData = selectReportData();
        PolicyDto policy = baseReportData.getPolicy();
        List<ProductOrderDto> productOrders = sourceDataMapper.selectProductOrders(policy.getPolicyNo(), policy.getPolicySeq());
        if (productOrders.isEmpty()) {
            throw new IllegalStateException("找不到保單商品訂單資料：lia_product_order.policy_no/policy_seq 必須等於保單，policy_no=" + policy.getPolicyNo() + ", policy_seq=" + policy.getPolicySeq());
        }
        return productOrders.stream()
                .map(productOrder -> productOrderReportData(baseReportData, productOrder))
                .toList();
    }

    private LiaReportData productOrderReportData(LiaReportData baseReportData, ProductOrderDto productOrder) {
        ProductDto product = sourceDataMapper.selectProductByProductCode(productOrder.getProductCode());
        if (product == null) {
            throw new IllegalStateException("找不到商品訂單對應商品：lia_product.product_code 必須等於 lia_product_order.product_code，product_code=" + productOrder.getProductCode());
        }
        return LiaReportData.builder()
                .policy(baseReportData.getPolicy())
                .customer(baseReportData.getCustomer())
                .productOrder(productOrder)
                .product(product)
                .payment(baseReportData.getPayment())
                .build();
    }
}
