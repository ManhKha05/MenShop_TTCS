package com.ttcs.menshop.modules.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Getter
@Setter
public class ReviewCreateRequest {

    @NotNull(message = "orderId không được để trống")
    private Integer orderId;

    @NotNull(message = "productId không được để trống")
    private Integer productId;

    @NotNull(message = "rating không được để trống")
    private Integer rating;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    private String content;

    private List<String> images;
}
