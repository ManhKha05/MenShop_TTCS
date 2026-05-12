package com.ttcs.menshop.modules.shipping.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PackageInfo {
    private Integer weight;
    private Integer length;
    private Integer width;
    private Integer height;
}
