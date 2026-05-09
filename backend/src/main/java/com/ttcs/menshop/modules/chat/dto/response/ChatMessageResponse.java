package com.ttcs.menshop.modules.chat.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private Integer id;
    private Integer roomId;

    private Integer senderId;
    private String senderName;
    private String senderAvatar;

    private String type;
    private String imageUrl;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
