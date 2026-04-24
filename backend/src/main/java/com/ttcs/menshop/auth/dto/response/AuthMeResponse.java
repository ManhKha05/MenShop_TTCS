package com.ttcs.menshop.auth.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthMeResponse {
    private Integer userId;
    private String fullname;
    private String email;
    private String avatar;
    private Integer shopId;
    private List<String> roles;
}