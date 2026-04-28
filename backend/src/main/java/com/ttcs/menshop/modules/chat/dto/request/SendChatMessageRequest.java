package com.ttcs.menshop.modules.chat.dto.request;

import lombok.Data;

@Data
public class SendChatMessageRequest {
    private Integer roomId;
    private String content;
}
