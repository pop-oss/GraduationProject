package com.erkang.controller;

import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.common.Result;
import com.erkang.domain.entity.Consultation;
import com.erkang.domain.enums.ConsultationStatus;
import com.erkang.integration.rtc.RTCToken;
import com.erkang.integration.rtc.RTCTokenService;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.security.Auditable;
import com.erkang.security.LoginUser;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * RTC视频通话控制器
 * _Requirements: 4.4, 4.5_
 */
@Slf4j
@RestController
@RequestMapping("/api/rtc")
@RequiredArgsConstructor
public class RTCController {

    private final RTCTokenService rtcTokenService;
    private final ConsultationMapper consultationMapper;

    /**
     * 获取RTC Token
     * 校验用户角色与问诊归属关系
     */
    @GetMapping("/token/{consultationId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "RTC_TOKEN_REQUEST", module = "consultation")
    public Result<RTCToken> getToken(@PathVariable Long consultationId) {
        Long userId = UserContext.getUserId();
        LoginUser loginUser = UserContext.getUser();
        // 获取第一个角色作为主角色
        String role = (loginUser != null && loginUser.getRoles() != null && !loginUser.getRoles().isEmpty()) 
                      ? loginUser.getRoles().get(0) : "UNKNOWN";
        
        // 校验问诊是否存在
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null) {
            throw new BusinessException(ErrorCode.CONSULT_NOT_FOUND, "问诊不存在");
        }
        
        // 校验用户是否属于该问诊
        boolean isPatient = consultation.getPatientId().equals(userId);
        boolean isDoctor = consultation.getDoctorId().equals(userId);
        if (!isPatient && !isDoctor) {
            throw new BusinessException(ErrorCode.CONSULT_NOT_BELONG, "无权访问该问诊");
        }
        
        // 校验问诊状态是否允许加入
        ConsultationStatus status = ConsultationStatus.fromCode(consultation.getStatus());
        if (status != ConsultationStatus.WAITING && status != ConsultationStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.CONSULT_STATUS_INVALID, "问诊状态不允许加入视频");
        }
        
        // 生成Token
        RTCToken token = rtcTokenService.generateToken(consultationId, userId, role);
        log.info("用户获取RTC Token: userId={}, consultationId={}", userId, consultationId);
        
        return Result.success(token);
    }

    /**
     * 加入房间通知
     */
    @PostMapping("/join/{consultationId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "RTC_JOIN", module = "consultation")
    public Result<Void> joinRoom(@PathVariable Long consultationId) {
        Long userId = UserContext.getUserId();
        log.info("用户加入RTC房间: userId={}, consultationId={}", userId, consultationId);
        return Result.success(null);
    }

    /**
     * 离开房间通知
     */
    @PostMapping("/leave/{consultationId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "RTC_LEAVE", module = "consultation")
    public Result<Void> leaveRoom(@PathVariable Long consultationId) {
        Long userId = UserContext.getUserId();
        log.info("用户离开RTC房间: userId={}, consultationId={}", userId, consultationId);
        return Result.success(null);
    }
}
