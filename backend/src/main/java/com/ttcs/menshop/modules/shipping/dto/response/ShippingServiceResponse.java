package com.ttcs.menshop.modules.shipping.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingServiceResponse {
    private Integer serviceId;
    private Integer serviceTypeId;
    private String name;
    private String description;
    private Integer fee;
    private String expectedDeliveryTime;
}
