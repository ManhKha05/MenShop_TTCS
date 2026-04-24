package com.ttcs.menshop.modules.category.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    private String name;
    private Integer parentId;
    private String imageUrl;
    private String status;
}
