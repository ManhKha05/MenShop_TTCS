package com.ttcs.menshop.modules.otp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Getter
@Setter
public class OtpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;
    private String otp;
    private LocalDateTime expired_at;
    private boolean used;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
