package com.ttcs.menshop.modules.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ShopReviewResponse {
    private Integer id;
    private String fullName;
    private String avatar;
    private String productName;
    private String productImage;
    private Integer rating;
    private String comment;
    private List<String> images;
    private String createdAt;
}
