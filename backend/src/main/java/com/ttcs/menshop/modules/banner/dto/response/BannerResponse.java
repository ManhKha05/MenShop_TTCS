package com.ttcs.menshop.modules.banner.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BannerResponse {
    private Integer id;
    private String title;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String status;
}
