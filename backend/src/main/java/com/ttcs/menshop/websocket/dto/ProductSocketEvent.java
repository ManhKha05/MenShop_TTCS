package com.ttcs.menshop.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSocketEvent {
    private String type;
    private Integer productId;
}
