package com.erkang.integration.rtc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RTC Token响应对象
 * _Requirements: 4.1_
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RTCToken {
    
    private String token;
    private String roomId;
    private String uid;
    private String appId;
    private long expireAt;
}
