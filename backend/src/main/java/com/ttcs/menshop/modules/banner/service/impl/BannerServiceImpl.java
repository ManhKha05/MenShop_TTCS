package com.ttcs.menshop.modules.banner.service.impl;

import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.banner.converter.BannerConverter;
import com.ttcs.menshop.modules.banner.dto.request.BannerRequest;
import com.ttcs.menshop.modules.banner.dto.response.BannerResponse;
import com.ttcs.menshop.modules.banner.dto.response.BannerStatsResponse;
import com.ttcs.menshop.modules.banner.entity.BannerEntity;
import com.ttcs.menshop.modules.banner.repository.BannerRepository;
import com.ttcs.menshop.modules.banner.service.BannerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final BannerConverter bannerConverter;

    public BannerServiceImpl(BannerRepository bannerRepository, BannerConverter bannerConverter) {
        this.bannerRepository = bannerRepository;
        this.bannerConverter = bannerConverter;
    }

    @Override
    public List<BannerResponse> getActiveBanners() {
        return bannerRepository.findByStatus("ACTIVE")
                .stream().map(bannerConverter::toResponse).toList();
    }

    @Override
    public Page<BannerResponse> getAllBanners(String keyword, String status, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return bannerRepository.search(keyword, status, pageRequest)
                .map(bannerConverter::toResponse);
    }

    @Override
    public BannerResponse createBanner(BannerRequest bannerRequest) {
        BannerEntity bannerEntity = bannerConverter.toEntity(bannerRequest);

        bannerEntity = bannerRepository.save(bannerEntity);
        return bannerConverter.toResponse(bannerEntity);
    }

    @Override
    public BannerResponse updateBanner(Integer id, BannerRequest bannerRequest) {
        BannerEntity banner = bannerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Banner không tồn tại với id = " + id));

        banner.setTitle(bannerRequest.getTitle());
        banner.setImageUrl(bannerRequest.getImageUrl());
        banner.setStatus(bannerRequest.getStatus());

        BannerEntity updated = bannerRepository.save(banner);
        return bannerConverter.toResponse(updated);
    }

    @Override
    public BannerStatsResponse getBannerStats() {
        Long total = bannerRepository.count();
        Long active = bannerRepository.countByStatus("ACTIVE");

        return new BannerStatsResponse(total, active);
    }
}
