package com.ttcs.menshop.modules.shop.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopRequest {
    private String name;
    private String description;
    private String logo;
    private String phone;
    private String email;
    private String address;
    private String detailAddress;
    private String wardId;
    private Integer districtId;
    private Integer provinceId;
}
