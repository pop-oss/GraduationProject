package com.erkang.domain;

import com.erkang.common.BusinessException;
import com.erkang.domain.entity.Prescription;
import com.erkang.domain.entity.PrescriptionItem;
import com.erkang.domain.enums.PrescriptionStatus;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.PatientProfileMapper;
import com.erkang.mapper.PrescriptionItemMapper;
import com.erkang.mapper.PrescriptionMapper;
import com.erkang.service.PrescriptionService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 处方状态流转属性测试
 * **Property 4: 处方审核状态流转**
 * **Validates: Requirements 6.2, 6.3, 6.4, 6.5, 6.7**
 */
class PrescriptionStatusPropertyTest {

    /**
     * Property 4.1: 草稿状态只能提交审核
     * *For any* prescription in DRAFT status, only PENDING_REVIEW transition is allowed
     */
    @Property(tries = 100)
    void draftCanOnlyTransitionToPendingReview(
            @ForAll @LongRange(min = 1, max = 1000000) Long prescriptionId) {
        
        PrescriptionStatus draft = PrescriptionStatus.DRAFT;
        
        // 验证只能转换到 PENDING_REVIEW
        assertThat(draft.canTransitionTo(PrescriptionStatus.PENDING_REVIEW)).isTrue();
        assertThat(draft.canTransitionTo(PrescriptionStatus.APPROVED)).isFalse();
        assertThat(draft.canTransitionTo(PrescriptionStatus.REJECTED)).isFalse();
        assertThat(draft.canTransitionTo(PrescriptionStatus.DISPENSED)).isFalse();
        assertThat(draft.canTransitionTo(PrescriptionStatus.DRAFT)).isFalse();
    }

    /**
     * Property 4.2: 待审核状态只能通过或驳回
     * *For any* prescription in PENDING_REVIEW status, only APPROVED or REJECTED transitions are allowed
     */
    @Property(tries = 100)
    void pendingReviewCanOnlyTransitionToApprovedOrRejected(
            @ForAll @LongRange(min = 1, max = 1000000) Long prescriptionId) {
        
        PrescriptionStatus pendingReview = PrescriptionStatus.PENDING_REVIEW;
        
        // 验证只能转换到 APPROVED 或 REJECTED
        assertThat(pendingReview.canTransitionTo(PrescriptionStatus.APPROVED)).isTrue();
        assertThat(pendingReview.canTransitionTo(PrescriptionStatus.REJECTED)).isTrue();
        assertThat(pendingReview.canTransitionTo(PrescriptionStatus.PENDING_REVIEW)).isFalse();
        assertThat(pendingReview.canTransitionTo(PrescriptionStatus.DRAFT)).isFalse();
        assertThat(pendingReview.canTransitionTo(PrescriptionStatus.DISPENSED)).isFalse();
    }

    /**
     * Property 4.3: 已通过状态只能发药
     * *For any* prescription in APPROVED status, only DISPENSED transition is allowed
     */
    @Property(tries = 100)
    void approvedCanOnlyTransitionToDispensed(
            @ForAll @LongRange(min = 1, max = 1000000) Long prescriptionId) {
        
        PrescriptionStatus approved = PrescriptionStatus.APPROVED;
        
        // 验证只能转换到 DISPENSED
        assertThat(approved.canTransitionTo(PrescriptionStatus.DISPENSED)).isTrue();
        assertThat(approved.canTransitionTo(PrescriptionStatus.APPROVED)).isFalse();
        assertThat(approved.canTransitionTo(PrescriptionStatus.REJECTED)).isFalse();
        assertThat(approved.canTransitionTo(PrescriptionStatus.PENDING_REVIEW)).isFalse();
        assertThat(approved.canTransitionTo(PrescriptionStatus.DRAFT)).isFalse();
    }

    /**
     * Property 4.4: 已驳回状态可以重新编辑
     * *For any* prescription in REJECTED status, can transition back to DRAFT
     */
    @Property(tries = 100)
    void rejectedCanTransitionToDraft(
            @ForAll @LongRange(min = 1, max = 1000000) Long prescriptionId) {
        
        PrescriptionStatus rejected = PrescriptionStatus.REJECTED;
        
        // 验证可以转换回 DRAFT
        assertThat(rejected.canTransitionTo(PrescriptionStatus.DRAFT)).isTrue();
        assertThat(rejected.canTransitionTo(PrescriptionStatus.APPROVED)).isFalse();
        assertThat(rejected.canTransitionTo(PrescriptionStatus.DISPENSED)).isFalse();
    }

    /**
     * Property 4.5: 已发药状态是终态
     * *For any* prescription in DISPENSED status, no transitions are allowed
     */
    @Property(tries = 100)
    void dispensedIsFinalState(
            @ForAll @LongRange(min = 1, max = 1000000) Long prescriptionId) {
        
        PrescriptionStatus dispensed = PrescriptionStatus.DISPENSED;
        
        // 验证是终态，不能转换到任何状态
        assertThat(dispensed.isFinal()).isTrue();
        assertThat(dispensed.getAllowedNextStatuses()).isEmpty();
        
        for (PrescriptionStatus status : PrescriptionStatus.values()) {
            assertThat(dispensed.canTransitionTo(status)).isFalse();
        }
    }

    /**
     * Property 4.6: 非法状态转换应被拒绝
     * *For any* illegal status transition, service should throw exception
     */
    @Property(tries = 100)
    void illegalTransitionShouldBeRejected(
            @ForAll @LongRange(min = 1, max = 1000000) Long prescriptionId,
            @ForAll("illegalTransitions") StatusTransition transition) {
        
        PrescriptionMapper prescriptionMapper = Mockito.mock(PrescriptionMapper.class);
        PrescriptionItemMapper itemMapper = Mockito.mock(PrescriptionItemMapper.class);
        ConsultationMapper consultationMapper = Mockito.mock(ConsultationMapper.class);
        PatientProfileMapper patientProfileMapper = Mockito.mock(PatientProfileMapper.class);
        PrescriptionService service = new PrescriptionService(prescriptionMapper, itemMapper, consultationMapper, patientProfileMapper);
        
        Prescription prescription = new Prescription();
        prescription.setId(prescriptionId);
        prescription.setStatus(transition.from.getCode());
        prescription.setPrescriptionNo("RX20241230TEST");
        
        when(prescriptionMapper.selectById(prescriptionId)).thenReturn(prescription);
        
        // 根据目标状态调用相应方法
        if (transition.to == PrescriptionStatus.APPROVED) {
            assertThatThrownBy(() -> service.approve(prescriptionId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("状态流转不合法");
        } else if (transition.to == PrescriptionStatus.REJECTED) {
            assertThatThrownBy(() -> service.reject(prescriptionId, "test"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("状态流转不合法");
        } else if (transition.to == PrescriptionStatus.DISPENSED) {
            assertThatThrownBy(() -> service.dispense(prescriptionId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("状态流转不合法");
        }
    }

    /**
     * Property 4.7: 只有草稿和驳回状态可编辑
     * *For any* prescription status, only DRAFT and REJECTED are editable
     */
    @Property(tries = 100)
    void onlyDraftAndRejectedAreEditable(
            @ForAll("allStatuses") PrescriptionStatus status) {
        
        boolean expectedEditable = (status == PrescriptionStatus.DRAFT || status == PrescriptionStatus.REJECTED);
        assertThat(status.isEditable()).isEqualTo(expectedEditable);
    }

    /**
     * Property 4.8: 提交审核需要处方明细
     * *For any* prescription without items, submit should fail
     */
    @Property(tries = 100)
    void submitRequiresPrescriptionItems(
            @ForAll @LongRange(min = 1, max = 1000000) Long prescriptionId) {
        
        PrescriptionMapper prescriptionMapper = Mockito.mock(PrescriptionMapper.class);
        PrescriptionItemMapper itemMapper = Mockito.mock(PrescriptionItemMapper.class);
        ConsultationMapper consultationMapper = Mockito.mock(ConsultationMapper.class);
        PatientProfileMapper patientProfileMapper = Mockito.mock(PatientProfileMapper.class);
        PrescriptionService service = new PrescriptionService(prescriptionMapper, itemMapper, consultationMapper, patientProfileMapper);
        
        Prescription prescription = new Prescription();
        prescription.setId(prescriptionId);
        prescription.setStatus(PrescriptionStatus.DRAFT.getCode());
        prescription.setPrescriptionNo("RX20241230TEST");
        
        when(prescriptionMapper.selectById(prescriptionId)).thenReturn(prescription);
        when(itemMapper.selectList(any())).thenReturn(Collections.emptyList());
        
        assertThatThrownBy(() -> service.submitForReview(prescriptionId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("处方明细不能为空");
    }

    @Provide
    Arbitrary<StatusTransition> illegalTransitions() {
        return Arbitraries.of(
            // DRAFT 不能直接到 APPROVED/REJECTED/DISPENSED
            new StatusTransition(PrescriptionStatus.DRAFT, PrescriptionStatus.APPROVED),
            new StatusTransition(PrescriptionStatus.DRAFT, PrescriptionStatus.REJECTED),
            new StatusTransition(PrescriptionStatus.DRAFT, PrescriptionStatus.DISPENSED),
            // PENDING_REVIEW 不能到 DISPENSED
            new StatusTransition(PrescriptionStatus.PENDING_REVIEW, PrescriptionStatus.DISPENSED),
            // APPROVED 不能到 REJECTED
            new StatusTransition(PrescriptionStatus.APPROVED, PrescriptionStatus.REJECTED),
            // DISPENSED 不能到任何状态
            new StatusTransition(PrescriptionStatus.DISPENSED, PrescriptionStatus.APPROVED),
            new StatusTransition(PrescriptionStatus.DISPENSED, PrescriptionStatus.REJECTED),
            new StatusTransition(PrescriptionStatus.DISPENSED, PrescriptionStatus.DISPENSED)
        );
    }

    @Provide
    Arbitrary<PrescriptionStatus> allStatuses() {
        return Arbitraries.of(PrescriptionStatus.values());
    }

    record StatusTransition(PrescriptionStatus from, PrescriptionStatus to) {}
}
