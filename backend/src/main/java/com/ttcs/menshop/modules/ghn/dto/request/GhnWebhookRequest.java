package com.ttcs.menshop.modules.ghn.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GhnWebhookRequest {

    @JsonProperty("OrderCode")
    private String orderCode;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("CODAmount")
    private Integer codAmount;

    @JsonProperty("Description")
    private String description;
}
