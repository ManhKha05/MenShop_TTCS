import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

let client = null;

export const connectSocket = (onConnected) => {
  client = new Client({
    webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
    reconnectDelay: 5000, // auto reconnect
    debug: (str) => {
      // console.log(str);
    },
    onConnect: () => {
      // console.log("WebSocket connected");
      onConnected?.(client);
    },
    onStompError: (frame) => {
      // console.error("Broker error:", frame.headers["message"]);
    },
  });

  client.activate();
};

export const disconnectSocket = () => {
  if (client) {
    client.deactivate();
  }
};

export const subscribeSocket = (destination, callback) => {
  if (!client || !client.connected) return null;

  return client.subscribe(destination, (message) => {
    const data = JSON.parse(message.body);
    callback?.(data);
  });
};

export const getClient = () => client;