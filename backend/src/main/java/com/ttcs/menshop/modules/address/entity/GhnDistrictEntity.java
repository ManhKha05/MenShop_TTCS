package com.ttcs.menshop.modules.address.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "ghn_district")
@Data
public class GhnDistrictEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer districtId;

    private String districtName;

    @Column(name = "province_id")
    private Integer provinceId;

}
