package com.ttcs.menshop.modules.otp.repository;

import com.ttcs.menshop.modules.otp.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Integer> {
    Optional<OtpEntity> findTopByEmailOrderByCreatedAtDesc(String email);
}
