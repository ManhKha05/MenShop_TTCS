package com.ttcs.menshop.modules.category.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryItemResponse {
    private Integer id;
    private String name;
    private String imageUrl;
    private Long totalProduct;
}
