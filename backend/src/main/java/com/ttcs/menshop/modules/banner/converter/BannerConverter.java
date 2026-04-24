package com.ttcs.menshop.modules.banner.converter;


import com.ttcs.menshop.modules.banner.dto.request.BannerRequest;
import com.ttcs.menshop.modules.banner.dto.response.BannerResponse;
import com.ttcs.menshop.modules.banner.entity.BannerEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BannerConverter {

    @Autowired
    private ModelMapper modelMapper;

    public BannerResponse toResponse(BannerEntity bannerEntity) {
        return modelMapper.map(bannerEntity, BannerResponse.class);
    }

    public BannerEntity toEntity(BannerRequest  bannerRequest) {
        return modelMapper.map(bannerRequest, BannerEntity.class);
    }
}
