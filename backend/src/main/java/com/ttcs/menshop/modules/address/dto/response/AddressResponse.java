package com.ttcs.menshop.modules.address.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    private Integer id;
    private String receiverName;
    private String phone;
    private String address;
    private String detailAddress;

    private Integer provinceId;
    private Integer districtId;
    private String wardId;

    private boolean isDefault;
}
