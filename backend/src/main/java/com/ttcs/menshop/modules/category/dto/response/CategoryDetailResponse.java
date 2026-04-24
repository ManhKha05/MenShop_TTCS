package com.ttcs.menshop.modules.category.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDetailResponse {
    private Integer id;
    private String name;
    private String imageUrl;

    private Integer parentId;
    private String parentName;

    private Boolean parentCategory;

    private String sectionTitle;
    private String sectionDescription;

    private List<CategoryItemResponse> categories;
}
