package com.ttcs.menshop.modules.banner.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BannerRequest {
    private String title;
    private String imageUrl;
    private String status;
}
