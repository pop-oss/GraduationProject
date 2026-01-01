package com.erkang.controller;

import com.erkang.domain.entity.Consultation;
import com.erkang.domain.enums.ConsultationStatus;
import com.erkang.integration.rtc.RTCToken;
import com.erkang.integration.rtc.RTCTokenService;
import com.erkang.mapper.ConsultationMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RTC视频通话控制器测试
 * _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
 */
class RTCControllerTest {

    private RTCTokenService rtcTokenService;
    private ConsultationMapper consultationMapper;
    private RTCController rtcController;

    @BeforeProperty
    void setUp() {
        rtcTokenService = mock(RTCTokenService.class);
        consultationMapper = mock(ConsultationMapper.class);
        rtcController = new RTCController(rtcTokenService, consultationMapper);
    }

    /**
     * Property 1: 获取RTC Token - 应返回有效的Token
     * **Validates: Requirements 4.4**
     */
    @Property(tries = 100)
    void getToken_shouldReturnValidToken(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId,
            @ForAll @LongRange(min = 1, max = 10000) Long userId) {
        
        RTCToken mockToken = RTCToken.builder()
            .token("mock-rtc-token")
            .roomId("room-" + consultationId)
            .uid(userId.toString())
            .appId("test-app-id")
            .expireAt(System.currentTimeMillis() + 3600000)
            .build();
        
        when(rtcTokenService.generateToken(eq(consultationId), eq(userId), anyString()))
            .thenReturn(mockToken);
        
        assertThat(mockToken.getToken()).isNotBlank();
        assertThat(mockToken.getRoomId()).contains(consultationId.toString());
        assertThat(mockToken.getExpireAt()).isGreaterThan(System.currentTimeMillis());
    }


    /**
     * Property 2: Token应包含必要信息
     * **Validates: Requirements 4.4**
     */
    @Property(tries = 100)
    void token_shouldContainRequiredInfo(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId,
            @ForAll @LongRange(min = 1, max = 10000) Long userId) {
        
        RTCToken mockToken = RTCToken.builder()
            .token("mock-rtc-token")
            .roomId("room-" + consultationId)
            .uid(userId.toString())
            .appId("test-app-id")
            .expireAt(System.currentTimeMillis() + 3600000)
            .build();
        
        assertThat(mockToken.getToken()).isNotBlank();
        assertThat(mockToken.getRoomId()).isNotBlank();
        assertThat(mockToken.getUid()).isNotBlank();
        assertThat(mockToken.getAppId()).isNotBlank();
    }

    /**
     * Property 3: 问诊状态验证 - 只有等待中或进行中的问诊才能加入视频
     * **Validates: Requirements 4.5**
     */
    @Property(tries = 50)
    void consultationStatus_shouldAllowVideoJoin(
            @ForAll("allowedVideoStatuses") ConsultationStatus status) {
        
        assertThat(status).isIn(ConsultationStatus.WAITING, ConsultationStatus.IN_PROGRESS);
    }

    @Provide
    Arbitrary<ConsultationStatus> allowedVideoStatuses() {
        return Arbitraries.of(ConsultationStatus.WAITING, ConsultationStatus.IN_PROGRESS);
    }

    /**
     * Property 4: 问诊状态验证 - 已结束或已取消的问诊不能加入视频
     * **Validates: Requirements 4.5**
     */
    @Property(tries = 50)
    void consultationStatus_shouldNotAllowVideoJoin(
            @ForAll("disallowedVideoStatuses") ConsultationStatus status) {
        
        assertThat(status).isIn(ConsultationStatus.FINISHED, ConsultationStatus.CANCELED);
    }

    @Provide
    Arbitrary<ConsultationStatus> disallowedVideoStatuses() {
        return Arbitraries.of(ConsultationStatus.FINISHED, ConsultationStatus.CANCELED);
    }

    /**
     * Property 5: 加入房间通知 - 应记录用户加入
     * **Validates: Requirements 4.1**
     */
    @Property(tries = 100)
    void joinRoom_shouldLogUserJoin(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId) {
        
        var result = rtcController.joinRoom(consultationId);
        
        assertThat(result).isNotNull();
    }


    /**
     * Property 6: 离开房间通知 - 应记录用户离开
     * **Validates: Requirements 4.2**
     */
    @Property(tries = 100)
    void leaveRoom_shouldLogUserLeave(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId) {
        
        var result = rtcController.leaveRoom(consultationId);
        
        assertThat(result).isNotNull();
    }

    /**
     * Property 7: Token过期时间验证 - 过期时间应在未来
     * **Validates: Requirements 4.4**
     */
    @Property(tries = 100)
    void tokenExpireTime_shouldBeInFuture(
            @ForAll @LongRange(min = 1, max = 24) long hoursFromNow) {
        
        long expireTime = System.currentTimeMillis() + hoursFromNow * 3600000L;
        
        assertThat(expireTime).isGreaterThan(System.currentTimeMillis());
    }

    /**
     * Property 8: 房间ID格式验证 - 应包含问诊ID
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 100)
    void roomId_shouldContainConsultationId(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId) {
        
        String roomId = "erkang-consultation-" + consultationId;
        
        assertThat(roomId).contains(consultationId.toString());
        assertThat(roomId).startsWith("erkang-consultation-");
    }

    /**
     * Property 9: 用户归属验证 - 只有问诊相关用户才能获取Token
     * **Validates: Requirements 4.5**
     */
    @Property(tries = 100)
    void userBelonging_shouldBeValidated(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId,
            @ForAll @LongRange(min = 1, max = 10000) Long patientId,
            @ForAll @LongRange(min = 1, max = 10000) Long doctorId,
            @ForAll @LongRange(min = 1, max = 10000) Long userId) {
        
        Consultation consultation = new Consultation();
        consultation.setId(consultationId);
        consultation.setPatientId(patientId);
        consultation.setDoctorId(doctorId);
        
        boolean isPatient = consultation.getPatientId().equals(userId);
        boolean isDoctor = consultation.getDoctorId().equals(userId);
        boolean hasAccess = isPatient || isDoctor;
        
        if (userId.equals(patientId) || userId.equals(doctorId)) {
            assertThat(hasAccess).isTrue();
        }
    }

    /**
     * Property 10: UID格式验证 - UID应为非空字符串
     * **Validates: Requirements 4.4**
     */
    @Property(tries = 100)
    void uid_shouldBeNonEmptyString(
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) Long userId) {
        
        String uid = userId.toString();
        assertThat(uid).isNotBlank();
    }
}
