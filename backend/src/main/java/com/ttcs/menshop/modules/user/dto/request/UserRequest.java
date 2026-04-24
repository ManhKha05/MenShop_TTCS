package com.ttcs.menshop.modules.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private String gender;
}
