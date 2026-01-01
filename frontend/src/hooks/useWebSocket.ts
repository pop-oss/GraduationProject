import { useEffect, useCallback, useState, useRef } from 'react';
import { wsClient, MessageType, WSMessage } from '@/services/websocket';
import { useAuthStore } from '@/store/useAuthStore';

/**
 * WebSocket Hook
 * _Requirements: 12.1, 12.2, 12.3, 12.4_
 */
export const useWebSocket = () => {
  const { isAuthed, accessToken, token } = useAuthStore();
  const [lastMessage, setLastMessage] = useState<WSMessage | null>(null);
  const unsubscribeRef = useRef<(() => void) | null>(null);

  // 登录后自动连接
  useEffect(() => {
    const authToken = accessToken || token;
    if (authToken && isAuthed) {
      wsClient.connect();
      // 订阅所有消息
      unsubscribeRef.current = wsClient.subscribe('*', (msg) => {
        setLastMessage(msg);
      });
    }
    // 组件卸载时取消订阅
    // _Requirements: 12.4_
    return () => {
      unsubscribeRef.current?.();
    };
  }, [isAuthed, accessToken, token]);

  // 订阅消息
  // _Requirements: 12.3_
  const subscribe = useCallback((type: string, handler: (message: WSMessage) => void) => {
    return wsClient.subscribe(type, handler);
  }, []);

  // 发送消息
  const send = useCallback((message: WSMessage) => {
    wsClient.send(message);
  }, []);

  // 发送聊天消息（兼容旧接口）
  const sendMessage = useCallback(
    (message: { type: string; consultationId: number; content: string; messageType: string }) => {
      wsClient.send({
        type: 'CHAT_MESSAGE' as MessageType,
        data: message,
        timestamp: Date.now(),
      });
    },
    []
  );

  return {
    subscribe,
    send,
    sendMessage,
    lastMessage,
    isConnected: wsClient.isConnected,
  };
};

/**
 * 订阅特定消息类型的 Hook
 * _Requirements: 12.3, 12.4_
 */
export const useWsSubscribe = (messageType: string, handler: (message: WSMessage) => void) => {
  useEffect(() => {
    const unsubscribe = wsClient.subscribe(messageType, handler);
    return () => {
      unsubscribe();
    };
  }, [messageType, handler]);
};

export default useWebSocket;
