package com.ttcs.menshop.modules.address.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {
    private String receiverName;
    private String phone;
    private String detailAddress;

    private Integer provinceId;
    private Integer districtId;
    private String wardId;

//    private Boolean isDefault;
}
