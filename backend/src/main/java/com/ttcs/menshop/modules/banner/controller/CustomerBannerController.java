package com.ttcs.menshop.modules.banner.controller;

import com.ttcs.menshop.modules.banner.dto.response.BannerResponse;
import com.ttcs.menshop.modules.banner.service.BannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/banners")
public class CustomerBannerController {

    private final BannerService bannerService;

    public CustomerBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public ResponseEntity<?> getActive(){
        List<BannerResponse> res = bannerService.getActiveBanners();
        return ResponseEntity.ok(res);
    }
}
