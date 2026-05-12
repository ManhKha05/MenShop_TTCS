package com.ttcs.menshop.modules.shipping.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GhnFeeResponse {
    private Integer code;
    private String message;
    private FeeData data;

    @Getter
    @Setter
    public static class FeeData {
        private Integer total;
        private Integer service_fee;
        private Integer insurance_fee;
    }
}