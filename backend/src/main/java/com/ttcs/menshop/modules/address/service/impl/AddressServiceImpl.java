package com.ttcs.menshop.modules.address.service.impl;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.address.converter.AddressConverter;
import com.ttcs.menshop.modules.address.dto.request.AddressRequest;
import com.ttcs.menshop.modules.address.dto.response.AddressResponse;
import com.ttcs.menshop.modules.address.entity.AddressEntity;
import com.ttcs.menshop.modules.address.entity.GhnDistrictEntity;
import com.ttcs.menshop.modules.address.entity.GhnProvinceEntity;
import com.ttcs.menshop.modules.address.entity.GhnWardEntity;
import com.ttcs.menshop.modules.address.repository.AddressRepository;
import com.ttcs.menshop.modules.address.repository.GhnDistrictRepository;
import com.ttcs.menshop.modules.address.repository.GhnProvinceRepository;
import com.ttcs.menshop.modules.address.repository.GhnWardRepository;
import com.ttcs.menshop.modules.address.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressConverter addressConverter;
    private final UserRepository userRepository;
    private final GhnProvinceRepository ghnProvinceRepository;
    private final GhnDistrictRepository ghnDistrictRepository;
    private final GhnWardRepository ghnWardRepository;

    public AddressServiceImpl(AddressRepository addressRepository, AddressConverter addressConverter, UserRepository userRepository, GhnProvinceRepository ghnProvinceRepository, GhnDistrictRepository ghnDistrictRepository, GhnWardRepository ghnWardRepository) {
        this.addressRepository = addressRepository;
        this.addressConverter = addressConverter;
        this.userRepository = userRepository;
        this.ghnProvinceRepository = ghnProvinceRepository;
        this.ghnDistrictRepository = ghnDistrictRepository;
        this.ghnWardRepository = ghnWardRepository;
    }

    @Override
    public List<AddressResponse> getAddressesByUserId(Integer userId) {
        List<AddressEntity> addressEntities = addressRepository.findByUserIdAndDeleted(userId, false);
        return addressEntities.stream().map(addressConverter::toResponse).toList();
    }

    @Transactional
    @Override
    public void createAddress(Integer userId, AddressRequest request) {
        UserEntity user = userRepository.findById(userId).get();

        GhnProvinceEntity province = ghnProvinceRepository.findById(request.getProvinceId())
                .orElseThrow(() -> new RuntimeException("Tỉnh/thành phố không hợp lệ"));

        GhnDistrictEntity district = ghnDistrictRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new RuntimeException("Quận/huyện không hợp lệ"));

        GhnWardEntity ward = ghnWardRepository.findById(request.getWardId())
                .orElseThrow(() -> new RuntimeException("Phường/xã không hợp lệ"));

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setReceiverName(request.getReceiverName());
        addressEntity.setPhone(request.getPhone());
        addressEntity.setDetailAddress(request.getDetailAddress());

        addressEntity.setProvinceId(request.getProvinceId());
        addressEntity.setDistrictId(request.getDistrictId());
        addressEntity.setWardId(request.getWardId());
        addressEntity.setUser(user);
        addressEntity.setAddress(buildFullAddress(
                request.getDetailAddress(),
                ward.getWardName(),
                district.getDistrictName(),
                province.getProvinceName()
        ));
        addressRepository.save(addressEntity);
    }

    @Transactional
    @Override
    public void updateAddress(Integer id, AddressRequest request) {
        AddressEntity entity = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa chỉ id: " + id));

        GhnProvinceEntity province = ghnProvinceRepository.findById(request.getProvinceId())
                .orElseThrow(() -> new RuntimeException("Tỉnh/thành phố không hợp lệ"));

        GhnDistrictEntity district = ghnDistrictRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new RuntimeException("Quận/huyện không hợp lệ"));

        GhnWardEntity ward = ghnWardRepository.findById(request.getWardId())
                .orElseThrow(() -> new RuntimeException("Phường/xã không hợp lệ"));

        entity.setReceiverName(request.getReceiverName());
        entity.setPhone(request.getPhone());
        entity.setDetailAddress(request.getDetailAddress());
        entity.setProvinceId(province.getProvinceId());
        entity.setDistrictId(district.getDistrictId());
        entity.setWardId(ward.getWardCode());
        entity.setAddress(buildFullAddress(
                request.getDetailAddress(),
                ward.getWardName(),
                district.getDistrictName(),
                province.getProvinceName()
        ));

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

    private String buildFullAddress(
            String detailAddress,
            String wardName,
            String districtName,
            String provinceName
    ) {
        return detailAddress + ", " + wardName + ", " + districtName + ", " + provinceName;
    }

}
