package com.ttcs.menshop.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class JwtResponse {
    private Integer userId;
    private String token;
    private String email;
    private String fullname;
    private String avatar;
    private Integer shopId;
    private List<String> roles = new ArrayList<>();
}
