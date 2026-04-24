package com.ttcs.menshop.modules.product.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private Integer id;
    private String image;      // lấy ảnh đầu tiên
    private String name;
    private String category;
    private String shop;
    private BigDecimal price;
    private Integer stock;     // tính từ variants
    private String status;
}
