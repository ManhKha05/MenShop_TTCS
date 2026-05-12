package com.ttcs.menshop.modules.shipping.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GhnAvailableServiceResponse {
    private Integer code;
    private String message;
    private List<ServiceData> data;

    @Getter
    @Setter
    public static class ServiceData {
        private Integer service_id;
        private String short_name;
        private Integer service_type_id;
    }
}