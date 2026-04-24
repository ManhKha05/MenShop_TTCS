package com.ttcs.menshop.modules.address.service;

import com.ttcs.menshop.modules.address.dto.request.AddressRequest;
import com.ttcs.menshop.modules.address.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getAddressesByUserId(Integer userId);
    void createAddress(Integer userId, AddressRequest addressRequest);
    void updateAddress(Integer id, AddressRequest addressRequest);
    void deleteAddress(Integer id);
    void setDefaultAddress(Integer userId, Integer addressId);
}
