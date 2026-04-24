package com.ttcs.menshop.auth.controller;

import com.ttcs.menshop.auth.dto.request.LoginRequest;
import com.ttcs.menshop.auth.dto.request.ResetPasswordRequest;
import com.ttcs.menshop.auth.dto.request.SendOtpRequest;
import com.ttcs.menshop.auth.dto.request.SignupRequest;
import com.ttcs.menshop.auth.dto.response.AuthMeResponse;
import com.ttcs.menshop.auth.dto.response.JwtResponse;
import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.jwt.JwtUtil;
import com.ttcs.menshop.modules.otp.dto.request.VerifyOtpRequest;
import com.ttcs.menshop.modules.otp.service.OtpService;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.user.dto.response.UserResponse;
import com.ttcs.menshop.modules.user.service.UserService;
import com.ttcs.menshop.security.CustomUserDetails;
import com.ttcs.menshop.security.CustomUserDetailsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthService authService;
    private final CustomUserDetailsService customUserDetailsService;
    private final OtpService otpService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService, AuthService authService,
                          CustomUserDetailsService customUserDetailsService, OtpService otpService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.authService = authService;
        this.customUserDetailsService = customUserDetailsService;
        this.otpService = otpService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            String accessToken = jwtUtil.generateToken(userDetails.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false) // local dev: false, production https: true
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return ResponseEntity.ok()
                    .body(new JwtResponse(
                            userDetails.getId(),
                            accessToken,
                            userDetails.getUsername(),
                            userDetails.getFullname(),
                            userDetails.getAvatar(),
                            userDetails.getUser().getShop().getId(),
                            userDetails.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.toList())
                    ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Tài khoản hoặc mật khẩu không chính xác"));
        }

    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().body(Map.of("message", "Đăng ký thành công"));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody SendOtpRequest request) {
        otpService.sendOtp(request.getEmail());
        return ResponseEntity.ok().body(Map.of("message", "OTP đã được gửi về email"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> sendOtp(@RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getOtp());
        String resetToken = jwtUtil.generateResetToken(request.getEmail());
        return ResponseEntity.ok().body(Map.of("resetToken", resetToken));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> sendOtp(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().body(Map.of("message", "Đổi mật khẩu thành công, vui lòng đăng nhập lại để tiếp tục!"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Không tìm thấy refresh token"));
        }

        try {
            String username = jwtUtil.extractUsername(refreshToken);

            UserDetails user = customUserDetailsService.loadUserByUsername(username);

            if (!jwtUtil.validateToken(refreshToken, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Refresh token không hợp lệ hoặc đã hết hạn"));
            }

            String newAccessToken = jwtUtil.generateToken(user.getUsername());

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh token không hợp lệ"));
        }
    }

    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Integer shopId = user.getShop() != null ? user.getShop().getId() : null;

        AuthMeResponse response = AuthMeResponse.builder()
                .userId(user.getId())
                .fullname(user.getFullName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .shopId(shopId)
                .roles(user.getRoles().stream().map(r -> "ROLE_" + r.getName()).toList())
                .build();

        return ResponseEntity.ok(response);
    }
}
