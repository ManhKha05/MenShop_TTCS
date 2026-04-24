package com.ttcs.menshop.modules.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryStatsResponse {
    private Long total;
    private Long active;
    private Long inactive;
    private Long parent;
}
