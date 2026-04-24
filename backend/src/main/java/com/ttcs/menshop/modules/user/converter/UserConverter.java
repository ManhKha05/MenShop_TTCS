package com.ttcs.menshop.modules.user.converter;

import com.ttcs.menshop.auth.entity.RoleEntity;
import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.modules.user.dto.response.UserResponse;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserConverter {

    private final ModelMapper modelMapper;

    public UserConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public UserResponse toResponse(UserEntity userEntity) {
        UserResponse userResponse = modelMapper.map(userEntity, UserResponse.class);
        List<String> roles = userEntity.getRoles()
                .stream().map(RoleEntity::getName)
                .toList();
        userResponse.setRoles(roles);
        return userResponse;
    }

}
