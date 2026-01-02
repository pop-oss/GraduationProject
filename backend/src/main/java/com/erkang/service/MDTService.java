package com.erkang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.domain.entity.MDTCase;
import com.erkang.domain.entity.MDTConclusion;
import com.erkang.domain.entity.MDTMember;
import com.erkang.mapper.MDTCaseMapper;
import com.erkang.mapper.MDTConclusionMapper;
import com.erkang.mapper.MDTMemberMapper;
import com.erkang.security.Auditable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * MDT会诊服务
 * _Requirements: 7.3, 7.4, 7.5, 7.6_
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MDTService {

    private final MDTCaseMapper mdtCaseMapper;
    private final MDTMemberMapper mdtMemberMapper;
    private final MDTConclusionMapper mdtConclusionMapper;

    /**
     * 发起会诊
     */
    @Transactional
    @Auditable(action = "CREATE_MDT", module = "mdt")
    public MDTCase createMDT(MDTCase mdtCase) {
        validateMDTData(mdtCase);
        
        mdtCase.setMdtNo(generateMDTNo());
        mdtCase.setStatus("PENDING");
        mdtCase.setStatusUpdatedAt(LocalDateTime.now());
        mdtCase.setCreatedAt(LocalDateTime.now());
        mdtCase.setUpdatedAt(LocalDateTime.now());
        
        mdtCaseMapper.insert(mdtCase);
        
        // 添加发起人为成员
        MDTMember initiator = new MDTMember();
        initiator.setMdtId(mdtCase.getId());
        initiator.setDoctorId(mdtCase.getInitiatorId());
        initiator.setRole("INITIATOR");
        initiator.setInviteStatus("ACCEPTED");
        initiator.setCreatedAt(LocalDateTime.now());
        initiator.setUpdatedAt(LocalDateTime.now());
        mdtMemberMapper.insert(initiator);
        
        log.info("发起会诊: mdtNo={}, initiator={}", mdtCase.getMdtNo(), mdtCase.getInitiatorId());
        return mdtCase;
    }

    /**
     * 校验会诊数据
     */
    private void validateMDTData(MDTCase mdtCase) {
        if (mdtCase.getTitle() == null || mdtCase.getTitle().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会诊主题为必填项");
        }
        // clinicalSummary 可选
    }

    /**
     * 邀请专家参会
     */
    @Transactional
    @Auditable(action = "INVITE_MDT_MEMBER", module = "mdt")
    public MDTMember inviteMember(Long mdtId, Long doctorId) {
        MDTCase mdtCase = mdtCaseMapper.selectById(mdtId);
        if (mdtCase == null) {
            throw new BusinessException(ErrorCode.MDT_NOT_FOUND);
        }
        
        // 检查是否已邀请
        LambdaQueryWrapper<MDTMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MDTMember::getMdtId, mdtId)
               .eq(MDTMember::getDoctorId, doctorId);
        if (mdtMemberMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该专家已被邀请");
        }
        
        MDTMember member = new MDTMember();
        member.setMdtId(mdtId);
        member.setDoctorId(doctorId);
        member.setRole("PARTICIPANT");
        member.setInviteStatus("PENDING");
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        mdtMemberMapper.insert(member);
        
        log.info("邀请专家参会: mdtNo={}, doctorId={}", mdtCase.getMdtNo(), doctorId);
        return member;
    }

    /**
     * 接受邀请
     */
    @Transactional
    @Auditable(action = "ACCEPT_MDT_INVITE", module = "mdt")
    public MDTMember acceptInvite(Long mdtId, Long doctorId) {
        MDTMember member = getMember(mdtId, doctorId);
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到邀请记录");
        }
        
        if (!"PENDING".equals(member.getInviteStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邀请状态不允许此操作");
        }
        
        member.setInviteStatus("ACCEPTED");
        member.setUpdatedAt(LocalDateTime.now());
        mdtMemberMapper.updateById(member);
        
        log.info("接受会诊邀请: mdtId={}, doctorId={}", mdtId, doctorId);
        return member;
    }

    /**
     * 拒绝邀请
     */
    @Transactional
    @Auditable(action = "REJECT_MDT_INVITE", module = "mdt")
    public MDTMember rejectInvite(Long mdtId, Long doctorId) {
        MDTMember member = getMember(mdtId, doctorId);
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到邀请记录");
        }
        
        if (!"PENDING".equals(member.getInviteStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邀请状态不允许此操作");
        }
        
        member.setInviteStatus("REJECTED");
        member.setUpdatedAt(LocalDateTime.now());
        mdtMemberMapper.updateById(member);
        
        log.info("拒绝会诊邀请: mdtId={}, doctorId={}", mdtId, doctorId);
        return member;
    }

    /**
     * 开始会诊
     */
    @Transactional
    @Auditable(action = "START_MDT", module = "mdt")
    public MDTCase startMDT(Long mdtId) {
        MDTCase mdtCase = mdtCaseMapper.selectById(mdtId);
        if (mdtCase == null) {
            throw new BusinessException(ErrorCode.MDT_NOT_FOUND);
        }
        
        if (!"PENDING".equals(mdtCase.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会诊状态不允许此操作");
        }
        
        mdtCase.setStatus("IN_PROGRESS");
        mdtCase.setActualStartTime(LocalDateTime.now());
        mdtCase.setStatusUpdatedAt(LocalDateTime.now());
        mdtCase.setRtcRoomId("MDT_" + mdtCase.getMdtNo());
        mdtCase.setUpdatedAt(LocalDateTime.now());
        mdtCaseMapper.updateById(mdtCase);
        
        log.info("开始会诊: mdtNo={}", mdtCase.getMdtNo());
        return mdtCase;
    }

    /**
     * 结束会诊
     */
    @Transactional
    @Auditable(action = "END_MDT", module = "mdt")
    public MDTCase endMDT(Long mdtId) {
        MDTCase mdtCase = mdtCaseMapper.selectById(mdtId);
        if (mdtCase == null) {
            throw new BusinessException(ErrorCode.MDT_NOT_FOUND);
        }
        
        if (!"IN_PROGRESS".equals(mdtCase.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会诊状态不允许此操作");
        }
        
        mdtCase.setStatus("COMPLETED");
        mdtCase.setActualEndTime(LocalDateTime.now());
        mdtCase.setStatusUpdatedAt(LocalDateTime.now());
        mdtCase.setUpdatedAt(LocalDateTime.now());
        mdtCaseMapper.updateById(mdtCase);
        
        log.info("结束会诊: mdtNo={}", mdtCase.getMdtNo());
        return mdtCase;
    }

    /**
     * 取消会诊
     */
    @Transactional
    @Auditable(action = "CANCEL_MDT", module = "mdt")
    public MDTCase cancelMDT(Long mdtId) {
        MDTCase mdtCase = mdtCaseMapper.selectById(mdtId);
        if (mdtCase == null) {
            throw new BusinessException(ErrorCode.MDT_NOT_FOUND);
        }
        
        if ("COMPLETED".equals(mdtCase.getStatus()) || "CANCELED".equals(mdtCase.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会诊状态不允许此操作");
        }
        
        mdtCase.setStatus("CANCELED");
        mdtCase.setStatusUpdatedAt(LocalDateTime.now());
        mdtCase.setUpdatedAt(LocalDateTime.now());
        mdtCaseMapper.updateById(mdtCase);
        
        log.info("取消会诊: mdtNo={}", mdtCase.getMdtNo());
        return mdtCase;
    }

    /**
     * 归档会诊结论
     */
    @Transactional
    @Auditable(action = "ARCHIVE_MDT_CONCLUSION", module = "mdt")
    public MDTConclusion archiveConclusion(MDTConclusion conclusion) {
        MDTCase mdtCase = mdtCaseMapper.selectById(conclusion.getMdtId());
        if (mdtCase == null) {
            throw new BusinessException(ErrorCode.MDT_NOT_FOUND);
        }
        
        if (!"COMPLETED".equals(mdtCase.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会诊未结束，无法归档结论");
        }
        
        // 检查是否已有结论
        LambdaQueryWrapper<MDTConclusion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MDTConclusion::getMdtId, conclusion.getMdtId());
        if (mdtConclusionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会诊结论已存在");
        }
        
        if (conclusion.getConclusion() == null || conclusion.getConclusion().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会诊结论为必填项");
        }
        
        conclusion.setCreatedAt(LocalDateTime.now());
        conclusion.setUpdatedAt(LocalDateTime.now());
        mdtConclusionMapper.insert(conclusion);
        
        log.info("归档会诊结论: mdtNo={}", mdtCase.getMdtNo());
        return conclusion;
    }

    /**
     * 会诊不可删除
     * _Requirements: 7.6_
     */
    public void deleteMDT(Long mdtId) {
        throw new BusinessException(ErrorCode.MDT_CANNOT_DELETE, "会诊记录不允许删除");
    }

    /**
     * 查询会诊详情
     */
    public MDTCase getById(Long mdtId) {
        return mdtCaseMapper.selectById(mdtId);
    }

    /**
     * 查询会诊成员
     */
    public List<MDTMember> getMembers(Long mdtId) {
        LambdaQueryWrapper<MDTMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MDTMember::getMdtId, mdtId);
        return mdtMemberMapper.selectList(wrapper);
    }

    /**
     * 查询会诊结论
     */
    public MDTConclusion getConclusion(Long mdtId) {
        LambdaQueryWrapper<MDTConclusion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MDTConclusion::getMdtId, mdtId);
        return mdtConclusionMapper.selectOne(wrapper);
    }

    /**
     * 查询医生发起的会诊
     */
    public List<MDTCase> listByInitiatorId(Long doctorId) {
        LambdaQueryWrapper<MDTCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MDTCase::getInitiatorId, doctorId)
               .orderByDesc(MDTCase::getCreatedAt);
        return mdtCaseMapper.selectList(wrapper);
    }

    /**
     * 查询医生参与的会诊
     */
    public List<MDTCase> listByDoctorId(Long doctorId) {
        LambdaQueryWrapper<MDTMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(MDTMember::getDoctorId, doctorId);
        List<MDTMember> members = mdtMemberMapper.selectList(memberWrapper);
        
        if (members.isEmpty()) {
            return List.of();
        }
        
        List<Long> mdtIds = members.stream().map(MDTMember::getMdtId).toList();
        LambdaQueryWrapper<MDTCase> caseWrapper = new LambdaQueryWrapper<>();
        caseWrapper.in(MDTCase::getId, mdtIds)
                   .orderByDesc(MDTCase::getCreatedAt);
        return mdtCaseMapper.selectList(caseWrapper);
    }

    /**
     * 查询患者相关会诊
     */
    public List<MDTCase> listByPatientId(Long patientId) {
        LambdaQueryWrapper<MDTCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MDTCase::getPatientId, patientId)
               .orderByDesc(MDTCase::getCreatedAt);
        return mdtCaseMapper.selectList(wrapper);
    }

    private MDTMember getMember(Long mdtId, Long doctorId) {
        LambdaQueryWrapper<MDTMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MDTMember::getMdtId, mdtId)
               .eq(MDTMember::getDoctorId, doctorId);
        return mdtMemberMapper.selectOne(wrapper);
    }

    private String generateMDTNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "MDT" + date + uuid;
    }
}
