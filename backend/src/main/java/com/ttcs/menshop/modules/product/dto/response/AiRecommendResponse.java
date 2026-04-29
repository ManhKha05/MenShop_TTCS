package com.ttcs.menshop.modules.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendResponse {
    private String status;
    private String message;
    private List<Integer> data;
}
