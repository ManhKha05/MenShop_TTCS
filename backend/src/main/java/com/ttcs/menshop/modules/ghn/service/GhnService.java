package com.ttcs.menshop.modules.ghn.service;

import com.ttcs.menshop.modules.ghn.dto.response.GhnCreateOrderResponse;
import com.ttcs.menshop.modules.order.entity.OrderEntity;

public interface GhnService {
    GhnCreateOrderResponse createOrder(OrderEntity order);
}
