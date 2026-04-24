package com.ttcs.menshop.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecentActivityResponse {
    private String type;
    private String title;
    private String createdAt;
}
