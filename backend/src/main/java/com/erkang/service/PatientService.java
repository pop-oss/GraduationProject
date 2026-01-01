package com.erkang.service;

import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.common.utils.DesensitizeUtil;
import com.erkang.domain.dto.PatientProfileDTO;
import com.erkang.domain.entity.PatientProfile;
import com.erkang.domain.entity.User;
import com.erkang.domain.vo.PatientProfileVO;
import com.erkang.mapper.PatientProfileMapper;
import com.erkang.mapper.UserMapper;
import com.erkang.security.DataScope;
import com.erkang.security.DataScopeHelper;
import com.erkang.security.DataScopeType;
import com.erkang.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 患者服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {
    
    private final PatientProfileMapper patientProfileMapper;
    private final UserMapper userMapper;
    
    /**
     * 获取当前患者档案
     */
    @DataScope(DataScopeType.PATIENT_SELF)
    public PatientProfileVO getMyProfile() {
        Long userId = UserContext.getUserId();
        return getProfileByUserId(userId);
    }
    
    /**
     * 根据用户ID获取患者档案（脱敏）
     */
    public PatientProfileVO getProfileByUserId(Long userId) {
        PatientProfile profile = patientProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }
        
        User user = userMapper.selectById(userId);
        return toVO(profile, user);
    }
    
    /**
     * 根据患者ID获取档案（供医生查看，需校验权限）
     */
    public PatientProfileVO getProfileById(Long patientId) {
        // 校验数据访问权限
        DataScopeHelper.checkPatientAccess(patientId);
        
        PatientProfile profile = patientProfileMapper.selectById(patientId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }
        
        User user = userMapper.selectById(profile.getUserId());
        return toVO(profile, user);
    }
    
    /**
     * 更新患者档案
     */
    @Transactional
    @DataScope(DataScopeType.PATIENT_SELF)
    public void updateProfile(PatientProfileDTO dto) {
        Long userId = UserContext.getUserId();
        
        PatientProfile profile = patientProfileMapper.selectByUserId(userId);
        if (profile == null) {
            // 首次创建档案
            profile = new PatientProfile();
            profile.setUserId(userId);
            BeanUtils.copyProperties(dto, profile);
            patientProfileMapper.insert(profile);
            log.info("创建患者档案: userId={}", userId);
        } else {
            // 更新档案
            BeanUtils.copyProperties(dto, profile);
            patientProfileMapper.updateById(profile);
            log.info("更新患者档案: userId={}", userId);
        }
    }

    /**
     * 转换为VO（脱敏）
     */
    private PatientProfileVO toVO(PatientProfile profile, User user) {
        PatientProfileVO vo = new PatientProfileVO();
        BeanUtils.copyProperties(profile, vo);
        
        if (user != null) {
            vo.setRealName(user.getRealName());
            // 脱敏处理
            vo.setPhone(DesensitizeUtil.desensitizePhone(user.getPhone()));
        }
        
        // 敏感信息脱敏
        vo.setIdCard(DesensitizeUtil.desensitizeIdCard(profile.getIdCard()));
        vo.setEmergencyPhone(DesensitizeUtil.desensitizePhone(profile.getEmergencyPhone()));
        
        return vo;
    }
    
    /**
     * 检查档案是否完整
     */
    public boolean isProfileComplete(Long userId) {
        PatientProfile profile = patientProfileMapper.selectByUserId(userId);
        if (profile == null) {
            return false;
        }
        return profile.getGender() != null && profile.getBirthDate() != null;
    }
}
