package com.ttcs.menshop.modules.address.service.impl;

import com.ttcs.menshop.modules.address.dto.response.GhnDistrictResponse;
import com.ttcs.menshop.modules.address.dto.response.GhnProvinceResponse;
import com.ttcs.menshop.modules.address.dto.response.GhnWardResponse;
import com.ttcs.menshop.modules.address.repository.GhnDistrictRepository;
import com.ttcs.menshop.modules.address.repository.GhnProvinceRepository;
import com.ttcs.menshop.modules.address.repository.GhnWardRepository;
import com.ttcs.menshop.modules.address.service.GhnAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GhnAddressAddressServiceImpl implements GhnAddressService {
    private final GhnProvinceRepository provinceRepository;
    private final GhnDistrictRepository districtRepository;
    private final GhnWardRepository wardRepository;

    public List<GhnProvinceResponse> getProvinces() {
        return provinceRepository.findAllByOrderByProvinceNameAsc()
                .stream()
                .map(item -> new GhnProvinceResponse(
                        item.getProvinceId(),
                        item.getProvinceName()
                ))
                .toList();
    }

    public List<GhnDistrictResponse> getDistricts(Integer provinceId) {
        return districtRepository.findByProvinceIdOrderByDistrictNameAsc(provinceId)
                .stream()
                .map(item -> new GhnDistrictResponse(
                        item.getDistrictId(),
                        item.getDistrictName()
                ))
                .toList();
    }

    public List<GhnWardResponse> getWards(Integer districtId) {
        return wardRepository.findByDistrictIdOrderByWardNameAsc(districtId)
                .stream()
                .map(item -> new GhnWardResponse(
                        item.getWardCode(),
                        item.getWardName()
                ))
                .toList();
    }
}
