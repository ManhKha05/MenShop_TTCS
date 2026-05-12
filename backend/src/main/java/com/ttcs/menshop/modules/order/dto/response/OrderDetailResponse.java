package com.ttcs.menshop.modules.order.dto.response;

import com.ttcs.menshop.modules.order_item.dto.response.OrderItemDetailResponse;
import com.ttcs.menshop.modules.order_status.dto.response.OrderStatusHistoryResponse;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDetailResponse {
    private Integer id;
    private String orderCode;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private String note;
    private BigDecimal totalPrice;
    private BigDecimal shippingFee;
    private BigDecimal finalPrice;
    private String status;
    private LocalDateTime createdAt;

    private String shippingServiceName;
    private String shippingStatus;
    private String ghnOrderCode;
    private LocalDateTime expectedDeliveryTime;

    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paidAt;

    List<OrderItemDetailResponse> orderDetails;
    List<OrderStatusHistoryResponse> orderStatusHistories;
}
