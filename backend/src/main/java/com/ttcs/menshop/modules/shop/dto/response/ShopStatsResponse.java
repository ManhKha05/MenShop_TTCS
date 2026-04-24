package com.ttcs.menshop.modules.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShopStatsResponse {
    private Long total;
    private Long request;
    private Long active;
    private Long banned;
}
