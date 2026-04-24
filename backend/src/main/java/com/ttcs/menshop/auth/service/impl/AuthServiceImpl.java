package com.ttcs.menshop.auth.service.impl;

import com.ttcs.menshop.auth.dto.request.ResetPasswordRequest;
import com.ttcs.menshop.auth.dto.request.SignupRequest;
import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.RoleRepository;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.email.EmailService;
import com.ttcs.menshop.exception.BadRequestException;
import com.ttcs.menshop.modules.otp.repository.OtpRepository;
import com.ttcs.menshop.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final OtpRepository otpRepository;

    public AuthServiceImpl(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder, RoleRepository roleRepository, OtpRepository otpRepository) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.otpRepository = otpRepository;
    }

    @Transactional
    @Override
    public void signup(SignupRequest request) {
        boolean existEmail = userRepository.existsByEmail(request.getEmail());
        if (existEmail) {
            throw new BadRequestException("Tài khoản đã tồn tại");
        }

//        boolean existUsername = userRepository.existsByUsername(request.getUsername());
//        if (existUsername) {
//            throw new BadRequestException("Tên đăng nhập đã tồn tại");
//        }

        UserEntity userEntity = new UserEntity();
        userEntity.setAvatar("https://res.cloudinary.com/dcjraarbb/image/upload/v1775368268/vj9ueejpcaablfaamdk7.webp");
        userEntity.setFullName(request.getFullname());
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setRoles(List.of(roleRepository.findByName("CUSTOMER").get()));
        userRepository.save(userEntity);
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        Claims claims;

        try {
            claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(request.getResetToken())
                    .getBody();
        } catch (Exception e) {
            throw new BadRequestException("Token không hợp lệ hoặc đã hết hạn");
        }

        if (!"RESET_PASSWORD".equals(claims.get("type"))) {
            throw new BadRequestException("Token không hợp lệ");
        }

        String email = claims.getSubject();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User không tồn tại"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

    @Override
    public UserEntity getCurrentUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadRequestException("Người dùng chưa đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        UserEntity user = userRepository.findById(5).orElse(null);
        return userDetails.getUser();
    }


}
