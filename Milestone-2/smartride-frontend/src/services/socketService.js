import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let stompClient = null;
let subscribers = []; // Keep track of multiple listeners

export const connectSocket = (userId) => {
  if (stompClient && stompClient.active) return;

  // Uses SockJS fallback for browsers without Websocket support
  const socket = new SockJS('http://localhost:8080/ws');
  
  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      console.log('Connected to WebSocket');
      // Subscribe to the unqiue user topic
      stompClient.subscribe(`/topic/user/${userId}`, (message) => {
        const notification = JSON.parse(message.body);
        // Call all functions listening to new messages
        subscribers.forEach((callback) => callback(notification));
      });
    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    },
  });

  stompClient.activate();
};

export const subscribeToNotifications = (callback) => {
  subscribers.push(callback);
  // Return an unsubscribe function
  return () => {
    subscribers = subscribers.filter((cb) => cb !== callback);
  };
};

export const disconnectSocket = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    subscribers = [];
  }
};
