package com.alinlin.liabatch.dto;

import com.alinlin.liabatch.entity.CustomerDto;
import com.alinlin.liabatch.entity.PaymentDto;
import com.alinlin.liabatch.entity.PolicyDto;
import com.alinlin.liabatch.entity.ProductOrderDto;
import com.alinlin.liabatch.entity.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LIA通報資料集合。
 * <p>
 * 表示產生一筆通報 TXT 所需的全部來源 DTO，並依 Excel 規格的 sourceFile 找到對應資料物件。
 * 固定值欄位直接由規格的 fixedValue 提供，不需要放進此資料集合。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiaReportData {
    private PolicyDto policy;
    private CustomerDto customer;
    private ProductOrderDto productOrder;
    private ProductDto product;
    private PaymentDto payment;

    public Object sourceObject(String sourceFile) {
        return switch (sourceFile) {
            case "POLICY" -> policy;
            case "CUSTOMER" -> customer;
            case "PRODUCT_ORDER" -> productOrder;
            case "PRODUCT" -> product;
            case "PAYMENT" -> payment;
            default -> throw new IllegalArgumentException("未知的 sourceFile：" + sourceFile);
        };
    }
}
