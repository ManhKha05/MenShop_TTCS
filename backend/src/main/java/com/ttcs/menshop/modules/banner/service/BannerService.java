package com.ttcs.menshop.modules.banner.service;

import com.ttcs.menshop.modules.banner.dto.request.BannerRequest;
import com.ttcs.menshop.modules.banner.dto.response.BannerResponse;
import com.ttcs.menshop.modules.banner.dto.response.BannerStatsResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BannerService {
    List<BannerResponse> getActiveBanners();
    Page<BannerResponse> getAllBanners(String keyword, String status, Integer page, Integer size);
    BannerResponse createBanner(BannerRequest bannerRequest);
    BannerResponse updateBanner(Integer id, BannerRequest bannerRequest);
    BannerStatsResponse  getBannerStats();
}
