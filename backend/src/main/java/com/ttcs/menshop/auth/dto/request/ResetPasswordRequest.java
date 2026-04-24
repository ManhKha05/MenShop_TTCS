package com.ttcs.menshop.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String resetToken;
    private String newPassword;
}
