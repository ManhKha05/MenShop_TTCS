package com.ttcs.menshop.modules.address.repository;

import com.ttcs.menshop.modules.address.entity.AddressEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<AddressEntity, Integer> {
    List<AddressEntity> findByUserIdAndDeleted(Integer userId, boolean deleted);

    @Modifying
    @Transactional
    @Query("UPDATE AddressEntity a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultByUserId(Integer userId);
}
