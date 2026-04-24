package com.ttcs.menshop.modules.address.converter;

import com.ttcs.menshop.modules.address.dto.request.AddressRequest;
import com.ttcs.menshop.modules.address.dto.response.AddressResponse;
import com.ttcs.menshop.modules.address.entity.AddressEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class AddressConverter {

    private final ModelMapper modelMapper;
    public AddressConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public AddressResponse toResponse(AddressEntity addressEntity) {
        return modelMapper.map(addressEntity, AddressResponse.class);
    }

    public AddressEntity toEntity(AddressRequest addressRequest) {
        return modelMapper.map(addressRequest, AddressEntity.class);
    }
}
