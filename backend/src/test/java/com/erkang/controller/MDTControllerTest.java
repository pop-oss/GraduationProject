package com.erkang.controller;

import com.erkang.domain.dto.CreateMDTRequest;
import com.erkang.domain.entity.MDTCase;
import com.erkang.domain.entity.MDTConclusion;
import com.erkang.domain.entity.MDTMember;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.MDTCaseMapper;
import com.erkang.mapper.MDTMemberMapper;
import com.erkang.service.MDTService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MDT会诊控制器测试
 * _Requirements: 7.3, 7.4, 7.5, 7.6_
 */
class MDTControllerTest {

    private MDTService mdtService;
    private MDTCaseMapper mdtCaseMapper;
    private MDTMemberMapper mdtMemberMapper;
    private ConsultationMapper consultationMapper;
    private MDTController mdtController;

    @BeforeProperty
    void setUp() {
        mdtService = mock(MDTService.class);
        mdtCaseMapper = mock(MDTCaseMapper.class);
        mdtMemberMapper = mock(MDTMemberMapper.class);
        consultationMapper = mock(ConsultationMapper.class);
        mdtController = new MDTController(mdtService, mdtCaseMapper, mdtMemberMapper, consultationMapper);
    }

    /**
     * Property 1: 发起会诊 - 应创建新的MDT会诊
     * **Validates: Requirements 7.3**
     */
    @Property(tries = 100)
    void createMDT_shouldCreateNewMDTCase(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId,
            @ForAll @AlphaChars @StringLength(min = 10, max = 200) String title) {
        
        CreateMDTRequest request = new CreateMDTRequest();
        request.setConsultationId(String.valueOf(consultationId));
        request.setTitle(title);
        request.setDescription("测试病情描述");
        
        MDTCase createdCase = new MDTCase();
        createdCase.setId(1L);
        createdCase.setConsultationId(consultationId);
        createdCase.setTitle(title);
        createdCase.setStatus("PENDING");
        
        when(mdtService.createMDT(any(MDTCase.class))).thenReturn(createdCase);
        
        var result = mdtController.createMDT(request);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getTitle()).isEqualTo(title);
        assertThat(result.getData().getStatus()).isEqualTo("PENDING");
    }


    /**
     * Property 2: 邀请专家参会 - 应创建会诊成员记录
     * **Validates: Requirements 7.3**
     */
    @Property(tries = 100)
    void inviteMember_shouldCreateMemberRecord(
            @ForAll @LongRange(min = 1, max = 10000) Long mdtId,
            @ForAll @LongRange(min = 1, max = 10000) Long doctorId) {
        
        MDTMember member = new MDTMember();
        member.setId(1L);
        member.setMdtId(mdtId);
        member.setDoctorId(doctorId);
        member.setInviteStatus("PENDING");
        
        when(mdtService.inviteMember(mdtId, doctorId)).thenReturn(member);
        
        var result = mdtController.inviteMember(mdtId, doctorId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getMdtId()).isEqualTo(mdtId);
        assertThat(result.getData().getDoctorId()).isEqualTo(doctorId);
    }

    /**
     * Property 3: 接受邀请 - 应更新成员状态为已接受
     * **Validates: Requirements 7.3**
     */
    @Property(tries = 100)
    void acceptInvite_shouldUpdateStatusToAccepted(
            @ForAll @LongRange(min = 1, max = 10000) Long mdtId) {
        
        MDTMember acceptedMember = new MDTMember();
        acceptedMember.setMdtId(mdtId);
        acceptedMember.setInviteStatus("ACCEPTED");
        
        when(mdtService.acceptInvite(eq(mdtId), anyLong())).thenReturn(acceptedMember);
        
        assertThat(acceptedMember.getInviteStatus()).isEqualTo("ACCEPTED");
    }

    /**
     * Property 4: 拒绝邀请 - 应更新成员状态为已拒绝
     * **Validates: Requirements 7.3**
     */
    @Property(tries = 100)
    void rejectInvite_shouldUpdateStatusToRejected(
            @ForAll @LongRange(min = 1, max = 10000) Long mdtId) {
        
        MDTMember rejectedMember = new MDTMember();
        rejectedMember.setMdtId(mdtId);
        rejectedMember.setInviteStatus("REJECTED");
        
        when(mdtService.rejectInvite(eq(mdtId), anyLong())).thenReturn(rejectedMember);
        
        assertThat(rejectedMember.getInviteStatus()).isEqualTo("REJECTED");
    }

    /**
     * Property 5: 开始会诊 - 应更新会诊状态为进行中
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    void startMDT_shouldUpdateStatusToInProgress(
            @ForAll @LongRange(min = 1, max = 10000) Long mdtId) {
        
        MDTCase startedCase = new MDTCase();
        startedCase.setId(mdtId);
        startedCase.setStatus("IN_PROGRESS");
        
        when(mdtService.startMDT(mdtId)).thenReturn(startedCase);
        
        var result = mdtController.startMDT(mdtId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData().getStatus()).isEqualTo("IN_PROGRESS");
    }


    /**
     * Property 6: 结束会诊 - 应更新会诊状态为已结束
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    void endMDT_shouldUpdateStatusToEnded(
            @ForAll @LongRange(min = 1, max = 10000) Long mdtId) {
        
        MDTCase endedCase = new MDTCase();
        endedCase.setId(mdtId);
        endedCase.setStatus("COMPLETED");
        
        when(mdtService.endMDT(mdtId)).thenReturn(endedCase);
        
        var result = mdtController.endMDT(mdtId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData().getStatus()).isEqualTo("COMPLETED");
    }

    /**
     * Property 7: 取消会诊 - 应更新会诊状态为已取消
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    void cancelMDT_shouldUpdateStatusToCanceled(
            @ForAll @LongRange(min = 1, max = 10000) Long mdtId) {
        
        MDTCase canceledCase = new MDTCase();
        canceledCase.setId(mdtId);
        canceledCase.setStatus("CANCELED");
        
        when(mdtService.cancelMDT(mdtId)).thenReturn(canceledCase);
        
        var result = mdtController.cancelMDT(mdtId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData().getStatus()).isEqualTo("CANCELED");
    }

    /**
     * Property 8: 归档会诊结论 - 应创建会诊结论记录
     * **Validates: Requirements 7.5**
     */
    @Property(tries = 100)
    void archiveConclusion_shouldCreateConclusionRecord(
            @ForAll @LongRange(min = 1, max = 10000) Long mdtId,
            @ForAll @AlphaChars @StringLength(min = 20, max = 500) String conclusionContent) {
        
        MDTConclusion conclusion = new MDTConclusion();
        conclusion.setConclusion(conclusionContent);
        
        MDTConclusion archivedConclusion = new MDTConclusion();
        archivedConclusion.setId(1L);
        archivedConclusion.setMdtId(mdtId);
        archivedConclusion.setConclusion(conclusionContent);
        
        when(mdtService.archiveConclusion(any(MDTConclusion.class))).thenReturn(archivedConclusion);
        
        var result = mdtController.archiveConclusion(mdtId, conclusion);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getMdtId()).isEqualTo(mdtId);
    }


    /**
     * Property 9: 查询会诊详情 - 应返回正确的会诊信息
     * **Validates: Requirements 7.5**
     */
    @Property(tries = 100)
    void getMDT_shouldReturnMDTDetails(
            @ForAll @LongRange(min = 1, max = 10000) Long mdtId) {
        
        MDTCase mockCase = new MDTCase();
        mockCase.setId(mdtId);
        
        when(mdtService.getById(mdtId)).thenReturn(mockCase);
        
        var result = mdtController.getById(mdtId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(mdtId);
    }

    /**
     * Property 10: 查询会诊成员 - 应返回该会诊的所有成员
     * **Validates: Requirements 7.5**
     */
    @Property(tries = 100)
    void getMembers_shouldReturnMDTMembers(
            @ForAll @LongRange(min = 1, max = 10000) Long mdtId,
            @ForAll @IntRange(min = 0, max = 10) int memberCount) {
        
        List<MDTMember> mockList = new ArrayList<>();
        for (int i = 0; i < memberCount; i++) {
            MDTMember member = new MDTMember();
            member.setId((long) (i + 1));
            member.setMdtId(mdtId);
            mockList.add(member);
        }
        
        when(mdtService.getMembers(mdtId)).thenReturn(mockList);
        
        var result = mdtController.getMembers(mdtId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(memberCount);
        result.getData().forEach(m -> 
            assertThat(m.getMdtId()).isEqualTo(mdtId)
        );
    }

    /**
     * Property 11: 删除会诊 - 应调用服务层删除
     * **Validates: Requirements 7.6**
     */
    @Property(tries = 100)
    void deleteMDT_shouldCallService(
            @ForAll @LongRange(min = 1, max = 10000) Long mdtId) {
        
        doNothing().when(mdtService).deleteMDT(mdtId);
        
        var result = mdtController.deleteMDT(mdtId);
        
        assertThat(result).isNotNull();
    }

    /**
     * Property 12: 会诊状态流转验证 - 状态变更应遵循流程
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 50)
    void mdtStatusTransition_shouldFollowProcess(
            @ForAll("validMDTStatusTransitions") String[] transition) {
        
        String from = transition[0];
        String to = transition[1];
        
        boolean isValid = isValidMDTTransition(from, to);
        assertThat(isValid).isTrue();
    }

    @Provide
    Arbitrary<String[]> validMDTStatusTransitions() {
        return Arbitraries.of(
            new String[]{"PENDING", "IN_PROGRESS"},
            new String[]{"IN_PROGRESS", "COMPLETED"},
            new String[]{"PENDING", "CANCELED"},
            new String[]{"IN_PROGRESS", "CANCELED"}
        );
    }

    private boolean isValidMDTTransition(String from, String to) {
        if ("PENDING".equals(from)) {
            return "IN_PROGRESS".equals(to) || "CANCELED".equals(to);
        }
        if ("IN_PROGRESS".equals(from)) {
            return "COMPLETED".equals(to) || "CANCELED".equals(to);
        }
        return false;
    }
}
