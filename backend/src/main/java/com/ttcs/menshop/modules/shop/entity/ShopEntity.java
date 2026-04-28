package com.ttcs.menshop.modules.shop.entity;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.modules.chat.entity.ChatRoomEntity;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import com.ttcs.menshop.modules.order_item.entity.OrderItemEntity;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shop")
@Getter
@Setter
public class ShopEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "logo")
    private String logo;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "createdAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status")
    private String status;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "shop")
    private List<ProductEntity> products;

    @OneToMany(mappedBy = "shop")
    private List<OrderEntity> orders;

    @OneToMany(mappedBy = "shop")
    private List<OrderItemEntity> orderItems;

    @OneToMany(mappedBy = "shop")
    private List<ChatRoomEntity> chatRooms;
}
