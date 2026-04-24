package com.ttcs.menshop.modules.banner.controller;

import com.ttcs.menshop.modules.banner.dto.request.BannerRequest;
import com.ttcs.menshop.modules.banner.dto.response.BannerResponse;
import com.ttcs.menshop.modules.banner.dto.response.BannerStatsResponse;
import com.ttcs.menshop.modules.banner.service.BannerService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/banners")
public class AdminBannerController {

    private final BannerService bannerService;
    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "6") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ){
        Page<BannerResponse> bannerResponse = bannerService.getAllBanners(keyword, status, page, size);
        return new ResponseEntity<>(bannerResponse, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BannerRequest request) {
        BannerResponse res = bannerService.createBanner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id , @RequestBody BannerRequest request) {
        BannerResponse res = bannerService.updateBanner(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        BannerStatsResponse bannerStatsResponse = bannerService.getBannerStats();
        return ResponseEntity.status(HttpStatus.OK).body(bannerStatsResponse);
    }

}
