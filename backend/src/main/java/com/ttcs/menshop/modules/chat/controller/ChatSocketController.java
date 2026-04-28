package com.ttcs.menshop.modules.chat.controller;

import com.ttcs.menshop.modules.chat.dto.request.SendChatMessageRequest;
import com.ttcs.menshop.modules.chat.dto.response.ChatMessageResponse;
import com.ttcs.menshop.modules.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(SendChatMessageRequest request) {
        ChatMessageResponse response = chatService.sendMessage(request);

        messagingTemplate.convertAndSend(
                "/topic/chat-room/" + request.getRoomId(),
                response
        );
    }
}
