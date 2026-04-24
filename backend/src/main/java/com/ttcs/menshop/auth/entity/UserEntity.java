package com.ttcs.menshop.auth.entity;

import com.ttcs.menshop.modules.address.entity.AddressEntity;
import com.ttcs.menshop.modules.cart_item.entity.CartItemEntity;
import com.ttcs.menshop.modules.notification.entity.NotificationEntity;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import com.ttcs.menshop.modules.review.entity.ReviewEntity;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user")
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", insertable = false)
    private String phone;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status", insertable = false)
    private String status;

    @Column(name = "gender", insertable = false)
    private String gender;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<RoleEntity> roles =  new ArrayList<>();

    @OneToOne(mappedBy = "user")
    private ShopEntity shop;

    @OneToMany(mappedBy = "user")
    private Set<AddressEntity> addresses = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private List<CartItemEntity> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<OrderEntity> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<NotificationEntity> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ReviewEntity>  reviews = new ArrayList<>();
}
