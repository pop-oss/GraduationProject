import { useAuthStore } from '@/store/useAuthStore';

export type MessageType =
  | 'CONSULTATION_STATUS'
  | 'PRESCRIPTION_STATUS'
  | 'MDT_INVITE'
  | 'FOLLOWUP_REMINDER'
  | 'SYSTEM_NOTICE'
  | 'CHAT_MESSAGE'
  | 'PING'
  | 'PONG';

export interface WSMessage {
  type: MessageType;
  messageType?: string;
  payload?: unknown;
  data?: unknown;
  timestamp: number;
  traceId?: string;
}

type MessageHandler = (message: WSMessage) => void;

/**
 * WebSocket 客户端
 * _Requirements: 12.1, 12.2, 12.3, 12.4_
 */
class WSClient {
  private ws: WebSocket | null = null;
  private url: string;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 10;
  private baseReconnectDelay = 1000; // 基础重连延迟 1 秒
  private maxReconnectDelay = 30000; // 最大重连延迟 30 秒
  private heartbeatInterval: number | null = null;
  private heartbeatTimeout: number | null = null;
  private handlers: Map<string, Set<MessageHandler>> = new Map();
  private isManualDisconnect = false;

  constructor(url: string) {
    this.url = url;
  }

  /**
   * 连接 WebSocket
   * _Requirements: 12.1_
   */
  connect(): void {
    const token = useAuthStore.getState().accessToken || useAuthStore.getState().token;
    if (!token) {
      console.warn('WebSocket: 未登录，无法连接');
      return;
    }

    // 如果已经连接，不重复连接
    if (this.ws?.readyState === WebSocket.OPEN) {
      return;
    }

    this.isManualDisconnect = false;
    const wsUrl = `${this.url}?token=${token}`;
    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      console.log('WebSocket: 连接成功');
      this.reconnectAttempts = 0;
      this.startHeartbeat();
    };

    this.ws.onmessage = (event) => {
      try {
        const message: WSMessage = JSON.parse(event.data);
        // 处理心跳响应
        if (message.type === 'PONG') {
          this.resetHeartbeatTimeout();
          return;
        }
        this.dispatchMessage(message);
      } catch (error) {
        console.error('WebSocket: 消息解析失败', error);
      }
    };

    this.ws.onclose = (event) => {
      console.log('WebSocket: 连接关闭', event.code, event.reason);
      this.stopHeartbeat();
      // 非手动断开时尝试重连
      if (!this.isManualDisconnect) {
        this.tryReconnect();
      }
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket: 连接错误', error);
    };
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.isManualDisconnect = true;
    this.stopHeartbeat();
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  /**
   * 发送消息
   */
  send(message: WSMessage): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket: 连接未就绪，无法发送消息');
    }
  }

  /**
   * 订阅消息
   * _Requirements: 12.3_
   */
  subscribe(type: string, handler: MessageHandler): () => void {
    if (!this.handlers.has(type)) {
      this.handlers.set(type, new Set());
    }
    this.handlers.get(type)!.add(handler);

    // 返回取消订阅函数
    // _Requirements: 12.4_
    return () => {
      this.handlers.get(type)?.delete(handler);
    };
  }

  /**
   * 分发消息
   * _Requirements: 12.3_
   */
  private dispatchMessage(message: WSMessage): void {
    // 根据 type 或 messageType 分发
    const messageType = message.messageType || message.type;
    const handlers = this.handlers.get(messageType);
    if (handlers) {
      handlers.forEach((handler) => handler(message));
    }

    // 同时分发到通配符处理器
    const wildcardHandlers = this.handlers.get('*');
    if (wildcardHandlers) {
      wildcardHandlers.forEach((handler) => handler(message));
    }
  }

  /**
   * 启动心跳
   */
  private startHeartbeat(): void {
    this.heartbeatInterval = window.setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'PING', timestamp: Date.now() }));
        this.startHeartbeatTimeout();
      }
    }, 30000); // 30秒心跳
  }

  /**
   * 启动心跳超时检测
   */
  private startHeartbeatTimeout(): void {
    this.heartbeatTimeout = window.setTimeout(() => {
      console.warn('WebSocket: 心跳超时，主动断开重连');
      this.ws?.close();
    }, 10000); // 10秒超时
  }

  /**
   * 重置心跳超时
   */
  private resetHeartbeatTimeout(): void {
    if (this.heartbeatTimeout) {
      clearTimeout(this.heartbeatTimeout);
      this.heartbeatTimeout = null;
    }
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
    this.resetHeartbeatTimeout();
  }

  /**
   * 尝试重连（指数退避）
   * _Requirements: 12.2_
   */
  private tryReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('WebSocket: 达到最大重连次数，停止重连');
      return;
    }

    this.reconnectAttempts++;
    // 指数退避：delay = baseDelay * 2^(attempts-1)，最大不超过 maxDelay
    const delay = Math.min(
      this.baseReconnectDelay * Math.pow(2, this.reconnectAttempts - 1),
      this.maxReconnectDelay
    );

    console.log(`WebSocket: ${delay}ms 后尝试第 ${this.reconnectAttempts} 次重连`);

    setTimeout(() => {
      this.connect();
    }, delay);
  }

  /**
   * 获取连接状态
   */
  get isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }
}

// 导出单例
const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws';
export const wsClient = new WSClient(wsUrl);
