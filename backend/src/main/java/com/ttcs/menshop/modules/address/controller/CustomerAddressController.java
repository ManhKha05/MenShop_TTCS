package com.ttcs.menshop.modules.address.controller;

import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.address.dto.request.AddressRequest;
import com.ttcs.menshop.modules.address.dto.response.AddressResponse;
import com.ttcs.menshop.modules.address.dto.response.GhnDistrictResponse;
import com.ttcs.menshop.modules.address.dto.response.GhnProvinceResponse;
import com.ttcs.menshop.modules.address.dto.response.GhnWardResponse;
import com.ttcs.menshop.modules.address.service.AddressService;
import com.ttcs.menshop.modules.address.service.GhnAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class CustomerAddressController {

    private final AddressService addressService;
    private final AuthService authService;
    private final GhnAddressService ghnAddressService;

    @GetMapping("/provinces")
    public List<GhnProvinceResponse> getProvinces() {
        return ghnAddressService.getProvinces();
    }

    @GetMapping("/districts")
    public List<GhnDistrictResponse> getDistricts(@RequestParam Integer provinceId) {
        return ghnAddressService.getDistricts(provinceId);
    }

    @GetMapping("/wards")
    public List<GhnWardResponse> getWards(@RequestParam Integer districtId) {
        return ghnAddressService.getWards(districtId);
    }

    @GetMapping()
    public ResponseEntity<?> getMyAddresses() {
        int userId = authService.getCurrentUser().getId();
        List<AddressResponse> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<?> addAddress(@RequestBody AddressRequest addressRequest) {
        int userId = authService.getCurrentUser().getId();
        addressService.createAddress(userId, addressRequest);
        return ResponseEntity.ok().body(Map.of("message", "Tạo địa chỉ thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable int id, @RequestBody AddressRequest addressRequest) {
        addressService.updateAddress(id, addressRequest);
        return ResponseEntity.ok().body(Map.of("message", "Cập nhật địa chỉ thành công"));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<?> setDefault(@PathVariable int id) {
        int userId = authService.getCurrentUser().getId();
        addressService.setDefaultAddress(userId, id);
        return ResponseEntity.ok(Map.of("message", "Đặt địa chỉ mặc định thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable int id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(Map.of("message", "Xóa địa chỉ thành công"));
    }
}
