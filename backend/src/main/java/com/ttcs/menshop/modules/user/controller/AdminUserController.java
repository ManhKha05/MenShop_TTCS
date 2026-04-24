package com.ttcs.menshop.modules.user.controller;

import com.ttcs.menshop.modules.user.dto.response.UserResponse;
import com.ttcs.menshop.modules.user.dto.response.UserStatsResponse;
import com.ttcs.menshop.modules.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        Page<UserResponse> responses = userService.getAllUsers(keyword, role, status, page, size);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        UserStatsResponse userStats = userService.getUserStats();
        return ResponseEntity.ok(userStats);
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<?> lock(@PathVariable Integer id) {
        userService.updateStatus(id, "LOCKED");
        return ResponseEntity.ok("User blocked");
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<?> unlock(@PathVariable Integer id) {
        userService.updateStatus(id, "ACTIVE");
        return ResponseEntity.ok("User unblocked");
    }

}
