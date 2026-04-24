package com.ttcs.menshop.modules.address.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressResponse {
    private Integer id;
    private String receiverName;
    private String phone;
    private String address;
    private boolean isDefault;
}
