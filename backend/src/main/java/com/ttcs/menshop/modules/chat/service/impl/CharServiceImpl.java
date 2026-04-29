package com.ttcs.menshop.modules.chat.service.impl;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.exception.BadRequestException;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.chat.dto.request.CreateChatRoomRequest;
import com.ttcs.menshop.modules.chat.dto.request.SendChatMessageRequest;
import com.ttcs.menshop.modules.chat.dto.response.ChatMessageResponse;
import com.ttcs.menshop.modules.chat.dto.response.ChatRoomResponse;
import com.ttcs.menshop.modules.chat.entity.ChatMessageEntity;
import com.ttcs.menshop.modules.chat.entity.ChatRoomEntity;
import com.ttcs.menshop.modules.chat.repository.ChatMessageRepository;
import com.ttcs.menshop.modules.chat.repository.ChatRoomRepository;
import com.ttcs.menshop.modules.chat.service.ChatService;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.shop.repository.ShopRepository;
import com.ttcs.menshop.websocket.service.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CharServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ShopRepository shopRepository;
    private final AuthService authService;
    private final WebSocketService webSocketService;

    @Transactional
    public ChatRoomResponse createOrGetRoom(CreateChatRoomRequest request) {
        UserEntity currentUser = authService.getCurrentUser();

        ShopEntity shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy shop"));

        if (shop.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không thể tự chat với shop của mình");
        }

        ChatRoomEntity room = chatRoomRepository
                .findByCustomerIdAndShopId(currentUser.getId(), shop.getId())
                .orElseGet(() -> chatRoomRepository.save(
                        ChatRoomEntity.builder()
                                .customer(currentUser)
                                .shop(shop)
                                .createdAt(LocalDateTime.now())
                                .build()
                ));

        return toRoomResponse(room, currentUser.getId());
    }

    @Override
    public List<ChatRoomResponse> getMyRooms() {
        UserEntity currentUser = authService.getCurrentUser();

        boolean isShop = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("SHOP"));

        List<ChatRoomEntity> rooms;

        if (isShop) {
            ShopEntity shop = shopRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Tài khoản này chưa có shop"));

            rooms = chatRoomRepository.findByShopIdOrderByLastMessageAtDesc(shop.getId());
        } else {
            rooms = chatRoomRepository.findByCustomerIdOrderByLastMessageAtDesc(currentUser.getId());
        }

        return rooms.stream()
                .map(room -> toRoomResponse(room, currentUser.getId()))
                .toList();
    }

    @Override
    public List<ChatMessageResponse> getMessages(Integer roomId) {
        UserEntity currentUser = authService.getCurrentUser();

        ChatRoomEntity room = getRoomAndCheckPermission(roomId, currentUser);

        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(room.getId())
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Override
    public ChatMessageResponse sendMessage(SendChatMessageRequest request) {
        UserEntity currentUser = authService.getCurrentUser();

        ChatRoomEntity room = getRoomAndCheckPermission(request.getRoomId(), currentUser);

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BadRequestException("Nội dung tin nhắn không được để trống");
        }

        ChatMessageEntity message =ChatMessageEntity.builder()
                        .room(room)
                        .sender(currentUser)
                        .content(request.getContent().trim())
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
        chatMessageRepository.save(message);

        room.setLastMessage(message.getContent());
        room.setLastMessageAt(message.getCreatedAt());
        chatRoomRepository.save(room);

        ChatMessageResponse response = toMessageResponse(message);

//        webSocketService.sendToTopic("/topic/chat-room/" + room.getId(), response);
        webSocketService.sendToTopic("/topic/chat-user/" + room.getCustomer().getId(), response);
        webSocketService.sendToTopic("/topic/chat-user/" + room.getShop().getUser().getId(), response);

        return response;
    }

    @Override
    public void markAsRead(Integer roomId) {
        UserEntity currentUser = authService.getCurrentUser();

        ChatRoomEntity room = getRoomAndCheckPermission(roomId, currentUser);

        List<ChatMessageEntity> messages = chatMessageRepository
                .findByRoomIdOrderByCreatedAtAsc(room.getId());

        messages.forEach(message -> {
            if (!message.getSender().getId().equals(currentUser.getId())) {
                message.setIsRead(true);
            }
        });

        chatMessageRepository.saveAll(messages);
    }

    private ChatRoomEntity getRoomAndCheckPermission(Integer roomId, UserEntity currentUser) {
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));

        boolean isCustomer = room.getCustomer().getId().equals(currentUser.getId());
        boolean isShopOwner = room.getShop().getUser().getId().equals(currentUser.getId());

        if (!isCustomer && !isShopOwner) {
            throw new RuntimeException("Bạn không có quyền truy cập phòng chat này");
        }

        return room;
    }

    private ChatRoomResponse toRoomResponse(ChatRoomEntity room, Integer currentUserId) {

        Integer unreadCount = chatMessageRepository
                .countByRoomIdAndIsReadFalseAndSenderIdNot(room.getId(), currentUserId);

        ChatMessageEntity lastMessage = chatMessageRepository
                .findFirstByRoomIdOrderByCreatedAtDesc(room.getId());

        Integer lastSenderId = null;
        String lastMessageContent = null;
        LocalDateTime lastMessageAt = null;

        if (lastMessage != null) {
            if (lastMessage.getSender() != null) {
                lastSenderId = lastMessage.getSender().getId();
            }
            lastMessageContent = lastMessage.getContent();
            lastMessageAt = lastMessage.getCreatedAt();
        } else {
            lastMessageContent = null;
            lastMessageAt = room.getCreatedAt();
        }

        return ChatRoomResponse.builder()
                .id(room.getId())

                .customerId(room.getCustomer().getId())
                .customerName(room.getCustomer().getFullName())
                .customerAvatar(room.getCustomer().getAvatar())

                .shopId(room.getShop().getId())
                .shopName(room.getShop().getName())
                .shopLogo(room.getShop().getLogo())

                .lastSenderId(lastSenderId)
                .lastMessage(lastMessageContent)
                .lastMessageAt(lastMessageAt)

                .unreadCount(unreadCount)
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessageEntity message) {
        boolean isShopSender = message.getSender().getId()
                .equals(message.getRoom().getShop().getUser().getId());

        String avatar;
        String name;

        if (isShopSender) {
            avatar = message.getRoom().getShop().getLogo();
            name = message.getRoom().getShop().getName();
        } else {
            avatar = message.getSender().getAvatar();
            name = message.getSender().getFullName();
        }

        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoom().getId())

                .senderId(message.getSender().getId())
                .senderName(name)
                .senderAvatar(avatar)

                .content(message.getContent())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
