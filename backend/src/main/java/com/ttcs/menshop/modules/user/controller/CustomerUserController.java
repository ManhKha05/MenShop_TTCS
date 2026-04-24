package com.ttcs.menshop.modules.user.controller;

import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.user.dto.request.UserRequest;
import com.ttcs.menshop.modules.user.dto.response.UserResponse;
import com.ttcs.menshop.modules.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class CustomerUserController {

    private final UserService userService;
    private final AuthService authService;

    public CustomerUserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile() {
        int userId = authService.getCurrentUser().getId();
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody UserRequest userRequest) {
        int userId = authService.getCurrentUser().getId();
        userService.updateProfile(userId, userRequest);
        return ResponseEntity.ok().body(Map.of("message", "Cập nhật thành công"));
    }
}
