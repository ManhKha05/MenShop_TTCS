package com.ttcs.menshop.modules.user.service;

import com.ttcs.menshop.auth.dto.request.SignupRequest;
import com.ttcs.menshop.modules.user.dto.request.UserRequest;
import com.ttcs.menshop.modules.user.dto.response.UserResponse;
import com.ttcs.menshop.modules.user.dto.response.UserStatsResponse;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserResponse> getAllUsers(String keyword, String role, String status, Integer page, Integer size);
    UserStatsResponse getUserStats();
    void updateStatus(Integer id, String status);
    UserResponse getUserById(Integer id);
    void updateProfile(Integer id, UserRequest userRequest);
}
