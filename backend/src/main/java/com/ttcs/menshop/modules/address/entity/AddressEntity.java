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

    @Column(name = "is_default", insertable = false)
    private boolean isDefault;

    @Column(name = "deleted", insertable = false)
    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
