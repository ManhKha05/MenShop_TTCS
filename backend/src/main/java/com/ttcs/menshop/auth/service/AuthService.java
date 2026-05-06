package com.ttcs.menshop.auth.service;

import com.ttcs.menshop.auth.dto.request.ResetPasswordRequest;
import com.ttcs.menshop.auth.dto.request.SignupRequest;
import com.ttcs.menshop.auth.entity.UserEntity;

public interface AuthService {
    void signup(SignupRequest request);
    void resetPassword(ResetPasswordRequest request);
    UserEntity getCurrentUser();
    UserEntity getCurrentUserOrNull();
}
