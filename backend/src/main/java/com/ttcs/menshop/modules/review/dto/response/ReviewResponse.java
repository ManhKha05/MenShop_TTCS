package com.ttcs.menshop.modules.review.dto.response;

import com.ttcs.menshop.modules.review_image.dto.response.ReviewImageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReviewResponse {
    private Integer id;
    private Integer userId;
    private String userName;
    private String userAvatar;
    private Integer productId;
    private Integer orderId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private List<ReviewImageResponse> images;
}
