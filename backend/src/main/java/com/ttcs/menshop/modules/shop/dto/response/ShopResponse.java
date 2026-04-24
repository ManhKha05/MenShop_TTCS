package com.ttcs.menshop.modules.shop.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ShopResponse {
    private int id;
    private String name;
    private String description;
    private String logo;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdAt;
    private String status;
    private String ownerName;
    private Integer totalProducts;
    private Long joinedDays;
    private Integer totalOrders;
}
