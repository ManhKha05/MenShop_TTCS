package com.ttcs.menshop.modules.address.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "ghn_ward")
@Data
public class GhnWardEntity {
    @Id
    @Column(name = "ward_code")
    private String wardCode;

    @Column(name = "ward_name")
    private String wardName;

    @Column(name = "district_id")
    private Integer districtId;
}
