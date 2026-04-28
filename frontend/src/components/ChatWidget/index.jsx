import React, { useEffect, useRef, useState } from "react";
import {
  Avatar, Badge, Button, Empty, Input,
  List,
  Spin,
  Typography,
  notification,
} from "antd";
import {
  MessageOutlined,
  CloseOutlined,
  SendOutlined,
} from "@ant-design/icons";
import { get, patch, post } from "../../utils/request"
import { HiChatBubbleLeftRight } from "react-icons/hi2";
import "./ChatWidget.scss";
import { connectSocket, disconnectSocket, subscribeSocket, getClient } from "../../utils/socket";

import { useChat } from "../ChatContext";

const { Text } = Typography;

export default function ChatWidget() {
  const { open, setOpen, activeRoomId, setActiveRoomId } = useChat();
  const [rooms, setRooms] = useState([]);
  const [activeRoom, setActiveRoom] = useState(null);
  const [messages, setMessages] = useState([]);
  const [content, setContent] = useState("");

  const [loadingRooms, setLoadingRooms] = useState(false);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);

  const bottomRef = useRef(null);

  const roles = JSON.parse(localStorage.getItem("roles") || "[]");

  const isShop = roles.includes("ROLE_SHOP");

  useEffect(() => {
    if (open) {
      fetchRooms();
    }
  }, [open]);

  useEffect(() => {
    if (activeRoom?.id) {
      fetchMessages(activeRoom.id);
      handleRead();
      setRooms((prev) =>
        prev.map((r) =>
          r.id === activeRoom.id
            ? { ...r, unreadCount: 0 }
            : r
        )
      );
    }
  }, [activeRoom]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const fetchRooms = async () => {
    try {
      setLoadingRooms(true);
      const res = await get("chat/rooms");
      const data = await res.json();

      setRooms(data || []);

      if (activeRoomId) {
        const found = data.find((r) => r.id === activeRoomId);
        if (found) {
          setActiveRoom(found);
        }
      } else if (data.length > 0) {
        setActiveRoom(data[0]);
      }
    } catch (error) {
      // notification.error({
      //   message: "Không tải được danh sách chat",
      // });
    } finally {
      setLoadingRooms(false);
    }
  };


  const fetchMessages = async (roomId) => {
    try {
      setLoadingMessages(true);
      const res = await get(`chat/rooms/${roomId}/messages`);

      const data = await res.json();

      setMessages(data || []);
    } catch (error) {
      notification.error({
        message: "Không tải được tin nhắn",
      });
    } finally {
      setLoadingMessages(false);
    }
  };

  const handleSend = async () => {
    if (!activeRoom) return;

    const text = content.trim();
    if (!text) return;

    try {
      setSending(true);
      const res = await post("chat/messages", {
        roomId: activeRoom.id,
        content: text
      });

      const newMessage = await res.json();

      // setMessages((prev) => [...prev, newMessage.senderId !== userId && newMessage]);
      setContent("");

      setRooms((prev) => {
        const updatedRooms = prev.map((room) =>
          room.id === newMessage.roomId
            ? {
              ...room,
              lastMessage: newMessage.content,
              lastMessageAt: newMessage.createdAt,
              unreadCount:
                activeRoom?.id === newMessage.roomId
                  ? 0
                  : (room.unreadCount || 0) + 1,
            }
            : room
        );

        const changedRoom = updatedRooms.find(
          (room) => room.id === newMessage.roomId
        );

        const otherRooms = updatedRooms.filter(
          (room) => room.id !== newMessage.roomId
        );

        return changedRoom
          ? [changedRoom, ...otherRooms]
          : updatedRooms;
      });
    } catch (error) {
      notification.error({
        message: "Gửi tin nhắn thất bại",
      });
    } finally {
      setSending(false);
    }
  };

  const handleRead = async () => {
    try {
      const res = await patch(`chat/rooms/${activeRoom.id}/read`);
    } catch (error) {
      console.error(error);
    }
  }

  const scrollToBottom = () => {
    setTimeout(() => {
      bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    }, 50);
  };

  const totalUnread = rooms.reduce(
    (sum, room) => sum + (room.unreadCount || 0),
    0
  );

  console.log("rooms", rooms);
  console.log("messages", messages)

  useEffect(() => {
    const userId = localStorage.getItem("userId");
    let subscription;

    connectSocket(() => {
      subscription = subscribeSocket(`/topic/chat-user/${userId}`, (newMessage) => {
        if (!newMessage) return;

        if (activeRoom?.id === newMessage.roomId) {
          setMessages((prev) => {
            const exists = prev.some((m) => m.id === newMessage.id);
            if (exists) return prev;
            return [...prev, newMessage];
          });

          patch(`chat/rooms/${newMessage.roomId}/read`);
        }

        setRooms((prev) => {
          const updatedRooms = prev.map((room) =>
            room.id === newMessage.roomId
              ? {
                ...room,
                lastMessage:
            newMessage.senderId === userId
              ? `Bạn: ${newMessage.content}`
              : newMessage.content,
                lastMessageAt: newMessage.createdAt,
                unreadCount:
                  activeRoom?.id === newMessage.roomId
                    ? 0
                    : (room.unreadCount || 0) + 1,
              }
              : room
          );

          const changedRoom = updatedRooms.find(
            (room) => room.id === newMessage.roomId
          );

          const otherRooms = updatedRooms.filter(
            (room) => room.id !== newMessage.roomId
          );

          return changedRoom
            ? [changedRoom, ...otherRooms]
            : updatedRooms;
        });
      });
    });

    return () => {
      subscription?.unsubscribe();
    };
  }, [open, activeRoom]);

  return (
    <>
      {!open && (
        <Badge count={totalUnread} offset={[-5, 5]}>
          <Button
            type="primary"
            // shape="circle"
            size="large"
            icon={<HiChatBubbleLeftRight />}
            className="chat-floating-btn"
            onClick={() => setOpen(true)}
          >
            Chat
          </Button>
        </Badge>
      )}

      {open && (
        <div className="chat-widget">
          <div className="chat-header">
            <div>
              <strong>Tin nhắn</strong>
              <div className="chat-header-sub">
                {isShop ? "Chat với khách hàng" : "Chat với shop"}
              </div>
            </div>

            <Button
              type="text"
              icon={<CloseOutlined />}
              onClick={() => setOpen(false)}
            />
          </div>

          <div className="chat-body">
            <div className="chat-room-list">
              {loadingRooms ? (
                <div className="chat-center">
                  <Spin />
                </div>
              ) : rooms.length === 0 ? (
                <Empty description="Chưa có đoạn chat" />
              ) : (
                <List
                  dataSource={rooms}
                  renderItem={(room) => {
                    const avatar = isShop ? room.customerAvatar : room.shopLogo;
                    const name = isShop ? room.customerName : room.shopName;

                    return (
                      <List.Item
                        className={
                          activeRoom?.id === room.id
                            ? "chat-room-item active"
                            : "chat-room-item"
                        }
                        onClick={() => setActiveRoom(room)}
                      >
                        <Badge count={room.unreadCount || 0}>
                          <Avatar src={avatar}>
                            {name?.charAt(0)}
                          </Avatar>
                        </Badge>

                        <div className="chat-room-info">
                          <div className="chat-room-name">{name}</div>
                          <div className="chat-room-row">
                            <div className="chat-room-last">
                              {room.lastMessage || "Chưa có tin nhắn"}
                            </div>

                          </div>
                        </div>
                      </List.Item>
                    );
                  }}
                />
              )}
            </div>

            <div className="chat-main">
              {!activeRoom ? (
                <div className="chat-center">
                  <Empty description="Chọn shop để chat" />
                </div>
              ) : (
                <>
                  <div className="chat-main-header">
                    <Avatar src={isShop ? activeRoom?.customerAvatar : activeRoom?.shopLogo}>
                      {(isShop ? activeRoom?.customerName : activeRoom?.shopName)?.charAt(0)}
                    </Avatar>

                    <strong>
                      {isShop ? activeRoom?.customerName : activeRoom?.shopName}
                    </strong>
                  </div>

                  <div className="chat-messages">
                    {loadingMessages ? (
                      <div className="chat-center">
                        <Spin />
                      </div>
                    ) : messages.length === 0 ? (
                      <Empty description="Chưa có tin nhắn" />
                    ) : (
                      messages.map((msg) => {
                        const myId = Number(localStorage.getItem("userId"));
                        const isMine = msg.senderId === myId;

                        return (
                          <div
                            key={msg.id}
                            className={
                              isMine
                                ? "chat-message-row mine"
                                : "chat-message-row"
                            }
                          >
                            {!isMine && (
                              <Avatar size={28} src={msg.senderAvatar}>
                                {msg.senderName?.charAt(0)}
                              </Avatar>
                            )}

                            <div className="chat-message-bubble">
                              <Text>{msg.content}</Text>
                            </div>
                          </div>
                        );
                      })
                    )}

                    <div ref={bottomRef} />
                  </div>

                  <div className="chat-input">
                    <Input.TextArea
                      value={content}
                      onChange={(e) => setContent(e.target.value)}
                      placeholder="Nhập tin nhắn..."
                      autoSize={{ minRows: 1, maxRows: 3 }}
                      onPressEnter={(e) => {
                        if (!e.shiftKey) {
                          e.preventDefault();
                          handleSend();
                        }
                      }}
                    />

                    <Button
                      type="primary"
                      icon={<SendOutlined />}
                      loading={sending}
                      onClick={handleSend}
                    />
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
}