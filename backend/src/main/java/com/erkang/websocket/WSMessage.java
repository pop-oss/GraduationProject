package com.erkang.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket消息体
 * _Requirements: 11.4_
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WSMessage {
    
    private WSMessageType type;
    private Object data;
    private LocalDateTime timestamp;
    
    public static WSMessage of(WSMessageType type, Object data) {
        return WSMessage.builder()
                       .type(type)
                       .data(data)
                       .timestamp(LocalDateTime.now())
                       .build();
    }
}
