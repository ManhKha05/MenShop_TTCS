package com.ttcs.menshop.modules.banner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BannerStatsResponse {
    private Long total;
    private Long active;
}
