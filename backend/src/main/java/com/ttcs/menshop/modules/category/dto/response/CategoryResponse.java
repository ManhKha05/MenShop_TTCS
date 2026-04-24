package com.ttcs.menshop.modules.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Integer id;
    private String name;
    private String imageUrl;
    private String status;
    private Integer parentId;
    private String parentName;
    private LocalDateTime createdAt;
//    private CategoryResponse parent;
//    private List<CategoryResponse> children;
}
