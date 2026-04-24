package com.ttcs.menshop.vnpay;

import com.ttcs.menshop.modules.order.entity.OrderEntity;
import com.ttcs.menshop.modules.payment_group.entity.PaymentGroupEntity;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VnPayService {
    String createPaymentUrl(PaymentGroupEntity payment, HttpServletRequest request);
    VnPayResponse handleReturn(Map<String, String> params);
    Map<String, String> handleIpn(Map<String, String> params);
}
