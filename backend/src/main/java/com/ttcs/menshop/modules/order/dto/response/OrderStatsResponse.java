package com.ttcs.menshop.modules.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatsResponse {
    private long total;
    private long pending;
    private long confirmed;
    private long shipping;
    private long delivered;
    private long cancelled;
}
