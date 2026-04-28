package com.ttcs.menshop.modules.chat.repository;

import com.ttcs.menshop.modules.chat.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Integer> {

    Optional<ChatRoomEntity> findByCustomerIdAndShopId(Integer customerId, Integer shopId);

    List<ChatRoomEntity> findByCustomerIdOrderByLastMessageAtDesc(Integer customerId);

    List<ChatRoomEntity> findByShopIdOrderByLastMessageAtDesc(Integer shopId);

}
