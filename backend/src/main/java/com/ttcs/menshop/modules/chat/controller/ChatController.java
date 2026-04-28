package com.ttcs.menshop.modules.chat.controller;

import com.ttcs.menshop.modules.chat.dto.request.CreateChatRoomRequest;
import com.ttcs.menshop.modules.chat.dto.request.SendChatMessageRequest;
import com.ttcs.menshop.modules.chat.dto.response.ChatMessageResponse;
import com.ttcs.menshop.modules.chat.dto.response.ChatRoomResponse;
import com.ttcs.menshop.modules.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ChatRoomResponse createOrGetRoom(@RequestBody CreateChatRoomRequest request) {
        return chatService.createOrGetRoom(request);
    }

    @GetMapping("/rooms")
    public List<ChatRoomResponse> getMyRooms() {
        return chatService.getMyRooms();
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Integer roomId) {
        return chatService.getMessages(roomId);
    }

    @PostMapping("/messages")
    public ChatMessageResponse sendMessage(@RequestBody SendChatMessageRequest request) {
        return chatService.sendMessage(request);
    }

    @PatchMapping("/rooms/{roomId}/read")
    public void markAsRead(@PathVariable Integer roomId) {
        chatService.markAsRead(roomId);
    }

}
