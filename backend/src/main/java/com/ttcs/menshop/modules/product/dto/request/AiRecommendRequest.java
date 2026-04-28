package com.ttcs.menshop.modules.product.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class AiRecommendRequest {
    private Integer user_id;
    @Builder.Default
    private List<Integer> viewed_product_ids = new ArrayList<>();
    private Integer target_product_id;
}
