package com.erkang.integration.rtc;

import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * RTC Token签发服务
 * _Requirements: 4.1, 4.2, 4.3_
 */
@Slf4j
@Service
public class RTCTokenService {

    @Value("${rtc.app-id:erkang-rtc}")
    private String appId;

    @Value("${rtc.app-secret:erkang-rtc-secret-key-for-token-generation}")
    private String appSecret;

    @Value("${rtc.token-expire-minutes:30}")
    private int tokenExpireMinutes;

    /**
     * 生成RTC Token
     * Token与consultationId、userId绑定，有效期30分钟
     */
    public RTCToken generateToken(Long consultationId, Long userId, String role) {
        if (consultationId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }

        String roomId = generateRoomId(consultationId);
        String uid = userId.toString();
        long expireAt = System.currentTimeMillis() + tokenExpireMinutes * 60 * 1000L;

        SecretKey key = Keys.hmacShaKeyFor(appSecret.getBytes(StandardCharsets.UTF_8));
        
        String token = Jwts.builder()
                .subject(uid)
                .claim("roomId", roomId)
                .claim("consultationId", consultationId)
                .claim("role", role)
                .claim("appId", appId)
                .issuedAt(new Date())
                .expiration(new Date(expireAt))
                .signWith(key)
                .compact();

        log.info("生成RTC Token: consultationId={}, userId={}, roomId={}", 
                consultationId, userId, roomId);

        return RTCToken.builder()
                      .token(token)
                      .roomId(roomId)
                      .uid(uid)
                      .appId(appId)
                      .expireAt(expireAt)
                      .build();
    }

    /**
     * 验证RTC Token
     */
    public boolean validateToken(String token, Long consultationId, Long userId) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(appSecret.getBytes(StandardCharsets.UTF_8));
            var claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

            Long tokenConsultationId = claims.get("consultationId", Long.class);
            String tokenUid = claims.getSubject();

            return consultationId.equals(tokenConsultationId) 
                   && userId.toString().equals(tokenUid);
        } catch (Exception e) {
            log.warn("RTC Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 生成房间ID
     */
    private String generateRoomId(Long consultationId) {
        return "room_" + consultationId;
    }
}
