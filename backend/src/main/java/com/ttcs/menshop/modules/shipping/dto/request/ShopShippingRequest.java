package com.ttcs.menshop.modules.shipping.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopShippingRequest {

    private Integer shopId;

    private Integer shippingServiceId;

    private String shippingServiceName;

    private BigDecimal fee;
}