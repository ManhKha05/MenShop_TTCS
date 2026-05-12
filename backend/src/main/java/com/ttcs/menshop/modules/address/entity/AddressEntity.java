package com.ttcs.menshop.modules.address.entity;


import com.ttcs.menshop.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "address")
@Getter
@Setter
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "ward_id")
    private String wardId;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "is_default", insertable = false)
    private boolean isDefault;

    @Column(name = "deleted", insertable = false)
    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
