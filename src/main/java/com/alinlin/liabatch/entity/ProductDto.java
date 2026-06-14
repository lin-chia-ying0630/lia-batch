package com.alinlin.liabatch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品資料 DTO。
 * <p>
 * 由 MySQL 的商品來源資料查出，供LIA通報規格中 sourceFile=PRODUCT 的欄位取值使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String productCode;
    private String productName;
}
