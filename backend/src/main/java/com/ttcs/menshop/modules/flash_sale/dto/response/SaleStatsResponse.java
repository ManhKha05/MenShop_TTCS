package com.ttcs.menshop.modules.flash_sale.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SaleStatsResponse {
    private Long total;
    private Long active;
    private Long upcoming;
    private Long ended;

}
