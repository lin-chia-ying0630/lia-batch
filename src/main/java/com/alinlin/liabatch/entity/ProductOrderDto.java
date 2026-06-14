package com.alinlin.liabatch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品訂單 DTO。
 * <p>
 * 由 MySQL 的 lia_product_order 查出，表示同一張保單底下第幾順位的商品。
 * 目前 LIA 通報只取 productOrderNo=1 的商品代碼來串 lia_product。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderDto {
    private Long productOrderId;
    private String policyNo;
    private String policySeq;
    private Integer productOrderNo;
    private String productCode;
}
