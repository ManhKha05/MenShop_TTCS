package com.ttcs.menshop.modules.shipping.controller;

import com.ttcs.menshop.modules.shipping.dto.request.ShippingServiceRequest;
import com.ttcs.menshop.modules.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/available-services")
    public ResponseEntity<?> getAvailableServices(
            @RequestBody ShippingServiceRequest request
    ) {
        return ResponseEntity.ok(shippingService.getAvailableServices(request));
    }
}
