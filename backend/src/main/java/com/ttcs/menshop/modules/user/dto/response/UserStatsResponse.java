package com.ttcs.menshop.modules.user.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsResponse {
    private Long total;
    private Long month;
    private Long shop;
    private Long customerActive;
}
