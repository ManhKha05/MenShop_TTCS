package com.ttcs.menshop.oauth;

import com.ttcs.menshop.auth.entity.RoleEntity;
import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.RoleRepository;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.exception.AccountLockedException;
import com.ttcs.menshop.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserServiceCustom {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserEntity processOAuthPostLogin(OAuth2User oAuth2User) {

        System.out.println("ATTRIBUTES = " + oAuth2User.getAttributes());

        String email = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");
        String avatar = oAuth2User.getAttribute("picture");

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Google không trả về email hợp lệ");
        }

        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    if ("LOCKED".equals(existingUser.getStatus())) {
                        throw new AccountLockedException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên để hỗ trợ!");
                    }

                    if (existingUser.getFullName() == null || existingUser.getFullName().isBlank()) {
                        existingUser.setFullName(fullName);
                    }
                    if (avatar != null && !avatar.isBlank()) {
                        existingUser.setAvatar(avatar);
                    }
//                    if (existingUser.getProvider() == null) {
//                        existingUser.setProvider(AuthProvider.GOOGLE);
//                    }
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(email);
                    newUser.setFullName(fullName);
                    newUser.setAvatar(avatar);
                    newUser.setPassword("");
                    newUser.setStatus("ACTIVE");
//                    newUser.setUsername(generateUniqueUsername(email));

                    RoleEntity customerRole = roleRepository.findByName("CUSTOMER")
                            .orElseThrow(() -> new RuntimeException("Chưa có ROLE_CUSTOMER trong DB"));

                    newUser.getRoles().add(customerRole);

                    return userRepository.save(newUser);
                });
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase();

        if (base.isBlank()) {
            base = "user";
        }

        String username = base;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = base + counter++;
        }

        return username;
    }
}
