import { createContext, useContext, useState } from "react";

const ChatContext = createContext();

export const ChatProvider = ({ children }) => {
  const [open, setOpen] = useState(false);
  const [activeRoomId, setActiveRoomId] = useState(null);

  const openChatWithRoom = (roomId) => {
    setActiveRoomId(roomId);
    setOpen(true);
  };

  return (
    <ChatContext.Provider
      value={{
        open,
        setOpen,
        activeRoomId,
        setActiveRoomId,
        openChatWithRoom,
      }}
    >
      {children}
    </ChatContext.Provider>
  );
};

export const useChat = () => useContext(ChatContext);