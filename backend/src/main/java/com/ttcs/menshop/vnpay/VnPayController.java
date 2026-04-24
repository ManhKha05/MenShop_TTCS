package com.ttcs.menshop.vnpay;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/vnpay")
public class VnPayController {

    private final VnPayService vnPayService;

    public VnPayController(VnPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @GetMapping("/return")
    public ResponseEntity<VnPayResponse> vnpayReturn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(vnPayService.handleReturn(params));
    }

    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(vnPayService.handleIpn(params));
    }
}
