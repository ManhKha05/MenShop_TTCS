package com.ttcs.menshop.modules.chat.service;

import com.ttcs.menshop.modules.chat.dto.request.CreateChatRoomRequest;
import com.ttcs.menshop.modules.chat.dto.request.SendChatMessageRequest;
import com.ttcs.menshop.modules.chat.dto.response.ChatMessageResponse;
import com.ttcs.menshop.modules.chat.dto.response.ChatRoomResponse;

import java.util.List;

public interface ChatService {
    ChatRoomResponse createOrGetRoom(CreateChatRoomRequest request);
    List<ChatRoomResponse> getMyRooms();
    List<ChatMessageResponse> getMessages(Integer roomId);
    ChatMessageResponse sendMessage(SendChatMessageRequest request);
    void markAsRead(Integer roomId);
}
