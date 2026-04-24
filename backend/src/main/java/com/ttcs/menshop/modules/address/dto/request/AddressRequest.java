package com.ttcs.menshop.modules.address.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {
    private String receiverName;
    private String phone;
    private String address;
}
