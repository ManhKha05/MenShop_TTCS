package com.ttcs.menshop.modules.user.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserResponse {
    private Integer id;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private LocalDateTime createdAt;
    private String status;
    private String gender;
    private List<String> roles = new ArrayList<>();
}
