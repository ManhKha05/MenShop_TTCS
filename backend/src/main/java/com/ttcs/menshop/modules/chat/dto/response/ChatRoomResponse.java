package com.ttcs.menshop.modules.chat.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatRoomResponse {
    private Integer id;

    private Integer customerId;
    private String customerName;
    private String customerAvatar;

    private Integer shopId;
    private String shopName;
    private String shopLogo;

    private Integer lastSenderId;
    private String lastMessage;
    private String lastMessageType;
    private LocalDateTime lastMessageAt;

    private Integer unreadCount;
}
