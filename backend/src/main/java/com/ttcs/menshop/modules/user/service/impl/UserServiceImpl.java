package com.ttcs.menshop.modules.user.service.impl;

import com.ttcs.menshop.auth.dto.request.SignupRequest;
import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.RoleRepository;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.exception.BadRequestException;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.user.converter.UserConverter;
import com.ttcs.menshop.modules.user.dto.request.UserRequest;
import com.ttcs.menshop.modules.user.dto.response.UserResponse;
import com.ttcs.menshop.modules.user.dto.response.UserStatsResponse;
import com.ttcs.menshop.modules.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, UserConverter userConverter, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }


    @Override
    public Page<UserResponse> getAllUsers(String keyword, String role, String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<UserEntity> userEntities = userRepository.search(keyword, role, status, pageable);
        Page<UserResponse> userResponses = userEntities
                .map(userConverter::toResponse);

        return userResponses;
    }

    @Override
    public UserStatsResponse getUserStats() {
        long total = userRepository.count();
        long currentMonth = userRepository.countCurrentMonth();
        long shop = userRepository.countByRoles_Name("SHOP");
        long activeCustomers = userRepository.countByRoles_NameAndStatus("CUSTOMER", "ACTIVE");
        return new UserStatsResponse(total, currentMonth, shop, activeCustomers);
    }

    @Override
    public void updateStatus(Integer id, String status) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng id: " + id));

        userEntity.setStatus(status);
        userRepository.save(userEntity);
    }

    @Override
    public UserResponse getUserById(Integer id) {

        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user id: " + id));
        UserResponse userResponse = userConverter.toResponse(userEntity);
        return userResponse;
    }

    @Override
    public void updateProfile(Integer id, UserRequest userRequest) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user id: " + id));

        user.setAvatar(userRequest.getAvatar());
        user.setFullName(userRequest.getFullName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setGender(userRequest.getGender());

        userRepository.save(user);
    }
}
