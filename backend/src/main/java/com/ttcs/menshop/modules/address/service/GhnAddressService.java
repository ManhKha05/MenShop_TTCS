package com.ttcs.menshop.modules.address.service;

import com.ttcs.menshop.modules.address.dto.response.GhnDistrictResponse;
import com.ttcs.menshop.modules.address.dto.response.GhnProvinceResponse;
import com.ttcs.menshop.modules.address.dto.response.GhnWardResponse;

import java.util.List;

public interface GhnAddressService {
    List<GhnProvinceResponse> getProvinces();
    List<GhnDistrictResponse> getDistricts(Integer provinceId);
    List<GhnWardResponse> getWards(Integer districtId);
}
