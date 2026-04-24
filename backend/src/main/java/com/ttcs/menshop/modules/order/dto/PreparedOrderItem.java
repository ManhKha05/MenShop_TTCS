package com.ttcs.menshop.modules.order.dto;

import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PreparedOrderItem {
    private Integer cartItemId;
    private ProductVariantEntity variant;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}