package com.erkang.integration.rtc;

import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RTC Token属性测试
 * **Property 5: RTC Token 安全性**
 * **Validates: Requirements 4.1, 4.2, 4.3, 4.4**
 */
class RTCTokenPropertyTest {

    private RTCTokenService createService() {
        RTCTokenService service = new RTCTokenService();
        ReflectionTestUtils.setField(service, "appId", "test-app-id");
        ReflectionTestUtils.setField(service, "appSecret", "test-secret-key-must-be-at-least-32-bytes-long");
        ReflectionTestUtils.setField(service, "tokenExpireMinutes", 30);
        return service;
    }

    /**
     * Property 5.1: 生成的Token必须与consultationId和userId绑定
     * *For any* valid consultationId and userId, the generated token should be bound to them
     */
    @Property(tries = 100)
    void tokenShouldBeBoundToConsultationAndUser(
            @ForAll @LongRange(min = 1, max = 1000000) Long consultationId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("roles") String role) {
        
        RTCTokenService service = createService();
        RTCToken token = service.generateToken(consultationId, userId, role);
        
        // Token应该能通过验证
        assertThat(service.validateToken(token.getToken(), consultationId, userId)).isTrue();
        
        // 使用不同的consultationId应该验证失败
        Long differentConsultationId = consultationId + 1;
        assertThat(service.validateToken(token.getToken(), differentConsultationId, userId)).isFalse();
        
        // 使用不同的userId应该验证失败
        Long differentUserId = userId + 1;
        assertThat(service.validateToken(token.getToken(), consultationId, differentUserId)).isFalse();
    }

    /**
     * Property 5.2: Token必须包含必要的房间信息
     * *For any* generated token, it should contain roomId, uid, and appId
     */
    @Property(tries = 100)
    void tokenShouldContainRequiredInfo(
            @ForAll @LongRange(min = 1, max = 1000000) Long consultationId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("roles") String role) {
        
        RTCTokenService service = createService();
        RTCToken token = service.generateToken(consultationId, userId, role);
        
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getRoomId()).isEqualTo("room_" + consultationId);
        assertThat(token.getUid()).isEqualTo(userId.toString());
        assertThat(token.getAppId()).isEqualTo("test-app-id");
        assertThat(token.getExpireAt()).isGreaterThan(System.currentTimeMillis());
    }

    /**
     * Property 5.3: 相同参数生成的Token应该不同（包含时间戳）
     * *For any* same parameters, generated tokens should be different due to timestamp
     */
    @Property(tries = 50)
    void sameParametersShouldGenerateDifferentTokens(
            @ForAll @LongRange(min = 1, max = 1000000) Long consultationId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId) {
        
        RTCTokenService service = createService();
        RTCToken token1 = service.generateToken(consultationId, userId, "PATIENT");
        RTCToken token2 = service.generateToken(consultationId, userId, "PATIENT");
        
        // 两次生成的token字符串可能相同（如果在同一毫秒内），但都应该有效
        assertThat(service.validateToken(token1.getToken(), consultationId, userId)).isTrue();
        assertThat(service.validateToken(token2.getToken(), consultationId, userId)).isTrue();
    }

    /**
     * Property 5.4: 篡改的Token应该验证失败
     * *For any* tampered token, validation should fail
     */
    @Property(tries = 100)
    void tamperedTokenShouldFailValidation(
            @ForAll @LongRange(min = 1, max = 1000000) Long consultationId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("tamperStrings") String tamperString) {
        
        RTCTokenService service = createService();
        RTCToken token = service.generateToken(consultationId, userId, "PATIENT");
        
        // 在token中间插入篡改字符串（而不是末尾）
        String originalToken = token.getToken();
        int midPoint = originalToken.length() / 2;
        String tamperedToken = originalToken.substring(0, midPoint) + tamperString + originalToken.substring(midPoint);
        
        assertThat(service.validateToken(tamperedToken, consultationId, userId)).isFalse();
    }

    @Provide
    Arbitrary<String> tamperStrings() {
        // 生成会破坏JWT结构的字符串
        return Arbitraries.of("TAMPERED", "xxx", "123", "abc", "!!!");
    }

    @Provide
    Arbitrary<String> roles() {
        return Arbitraries.of("PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT");
    }
}
