package com.ttcs.menshop.modules.shipping.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GhnLeadTimeResponse {
    private Integer code;
    private String message;
    private LeadTimeData data;

    @Getter
    @Setter
    public static class LeadTimeData {
        private Long leadtime;
    }
}