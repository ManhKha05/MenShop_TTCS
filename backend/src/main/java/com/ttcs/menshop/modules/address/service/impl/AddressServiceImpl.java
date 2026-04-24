package com.ttcs.menshop.modules.address.service.impl;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.address.converter.AddressConverter;
import com.ttcs.menshop.modules.address.dto.request.AddressRequest;
import com.ttcs.menshop.modules.address.dto.response.AddressResponse;
import com.ttcs.menshop.modules.address.entity.AddressEntity;
import com.ttcs.menshop.modules.address.repository.AddressRepository;
import com.ttcs.menshop.modules.address.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressConverter addressConverter;
    private final UserRepository userRepository;

    public AddressServiceImpl(AddressRepository addressRepository, AddressConverter addressConverter, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.addressConverter = addressConverter;
        this.userRepository = userRepository;
    }

    @Override
    public List<AddressResponse> getAddressesByUserId(Integer userId) {
        List<AddressEntity> addressEntities = addressRepository.findByUserIdAndDeleted(userId, false);
        return addressEntities.stream().map(addressConverter::toResponse).toList();
    }

    @Transactional
    @Override
    public void createAddress(Integer userId, AddressRequest addressRequest) {
        UserEntity user = userRepository.findById(userId).get();
        AddressEntity addressEntity = addressConverter.toEntity(addressRequest);
        addressEntity.setUser(user);
        addressRepository.save(addressEntity);
    }

    @Transactional
    @Override
    public void updateAddress(Integer id, AddressRequest addressRequest) {
        AddressEntity entity = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa chỉ id: " + id));

        entity.setReceiverName(addressRequest.getReceiverName());
        entity.setAddress(addressRequest.getAddress());
        entity.setPhone(addressRequest.getPhone());

        addressRepository.save(entity);
    }

    @Transactional
    @Override
    public void deleteAddress(Integer id) {
        AddressEntity address = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa chỉ id: " + id));

        address.setDeleted(true);
        addressRepository.save(address);
    }

    @Transactional
    @Override
    public void setDefaultAddress(Integer userId, Integer addressId) {
        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa chỉ id: " + addressId));

        addressRepository.clearDefaultByUserId(userId);
        address.setDefault(true);
        addressRepository.save(address);
    }
}
