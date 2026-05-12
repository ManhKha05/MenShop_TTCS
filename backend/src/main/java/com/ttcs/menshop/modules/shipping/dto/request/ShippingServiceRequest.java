package com.ttcs.menshop.modules.shipping.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ShippingServiceRequest {
    private Integer shopId;

    private Integer toDistrictId;
    private String toWardCode;
    private Integer insuranceValue;

    private List<ItemRequest> items;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemRequest {
        private Integer productId;
        private Integer quantity;
    }
}
