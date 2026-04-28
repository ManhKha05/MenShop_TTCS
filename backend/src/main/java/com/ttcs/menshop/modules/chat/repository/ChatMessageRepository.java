package com.ttcs.menshop.modules.chat.repository;


import com.ttcs.menshop.modules.chat.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Integer> {

    List<ChatMessageEntity> findByRoomIdOrderByCreatedAtAsc(Integer roomId);

    Integer countByRoomIdAndIsReadFalseAndSenderIdNot(Integer roomId, Integer senderId);

    ChatMessageEntity findFirstByRoomIdOrderByCreatedAtDesc(Integer roomId);
}
