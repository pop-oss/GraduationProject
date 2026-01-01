package com.erkang.domain;

import com.erkang.domain.enums.ConsultationStatus;
import net.jqwik.api.*;

import java.util.List;

/**
 * 问诊状态机属性测试
 * 
 * Feature: ent-telemedicine, Property 3: 问诊状态机合法性
 * Validates: Requirements 3.3, 3.4, 3.7
 */
class ConsultationStatusPropertyTest {
    
    /**
     * Property 1: WAITING状态只能转换到IN_PROGRESS或CANCELED
     */
    @Property(tries = 100)
    @Label("WAITING状态只能转换到IN_PROGRESS或CANCELED")
    void waitingCanOnlyTransitionToInProgressOrCanceled(
            @ForAll("allStatuses") ConsultationStatus targetStatus
    ) {
        ConsultationStatus waiting = ConsultationStatus.WAITING;
        boolean canTransition = waiting.canTransitionTo(targetStatus);
        
        if (targetStatus == ConsultationStatus.IN_PROGRESS || 
            targetStatus == ConsultationStatus.CANCELED) {
            assert canTransition : "WAITING应该能转换到" + targetStatus;
        } else {
            assert !canTransition : "WAITING不应该能转换到" + targetStatus;
        }
    }
    
    /**
     * Property 2: IN_PROGRESS状态只能转换到FINISHED或CANCELED
     */
    @Property(tries = 100)
    @Label("IN_PROGRESS状态只能转换到FINISHED或CANCELED")
    void inProgressCanOnlyTransitionToFinishedOrCanceled(
            @ForAll("allStatuses") ConsultationStatus targetStatus
    ) {
        ConsultationStatus inProgress = ConsultationStatus.IN_PROGRESS;
        boolean canTransition = inProgress.canTransitionTo(targetStatus);
        
        if (targetStatus == ConsultationStatus.FINISHED || 
            targetStatus == ConsultationStatus.CANCELED) {
            assert canTransition : "IN_PROGRESS应该能转换到" + targetStatus;
        } else {
            assert !canTransition : "IN_PROGRESS不应该能转换到" + targetStatus;
        }
    }
    
    /**
     * Property 3: 终态(FINISHED/CANCELED)不能转换到任何状态
     */
    @Property(tries = 100)
    @Label("终态不能转换到任何状态")
    void finalStatesCannotTransition(
            @ForAll("finalStatuses") ConsultationStatus finalStatus,
            @ForAll("allStatuses") ConsultationStatus targetStatus
    ) {
        boolean canTransition = finalStatus.canTransitionTo(targetStatus);
        assert !canTransition : finalStatus + "是终态，不应该能转换到" + targetStatus;
    }
    
    /**
     * Property 4: 终态的getAllowedNextStatuses应该返回空列表
     */
    @Property(tries = 100)
    @Label("终态的下一状态列表为空")
    void finalStatesHaveEmptyNextStatuses(
            @ForAll("finalStatuses") ConsultationStatus finalStatus
    ) {
        List<ConsultationStatus> nextStatuses = finalStatus.getAllowedNextStatuses();
        assert nextStatuses.isEmpty() : finalStatus + "的下一状态列表应该为空";
    }
    
    /**
     * Property 5: isFinal方法正确识别终态
     */
    @Property(tries = 100)
    @Label("isFinal方法正确识别终态")
    void isFinalCorrectlyIdentifiesFinalStates(
            @ForAll("allStatuses") ConsultationStatus status
    ) {
        boolean isFinal = status.isFinal();
        boolean shouldBeFinal = (status == ConsultationStatus.FINISHED || 
                                 status == ConsultationStatus.CANCELED);
        
        assert isFinal == shouldBeFinal : 
            status + "的isFinal()应该返回" + shouldBeFinal;
    }

    /**
     * Property 6: 状态转换的对称性 - 如果A不能转到B，则这是设计决定
     * 验证状态机的完整性
     */
    @Property(tries = 100)
    @Label("状态转换符合业务规则")
    void stateTransitionsFollowBusinessRules(
            @ForAll("nonFinalStatuses") ConsultationStatus fromStatus,
            @ForAll("allStatuses") ConsultationStatus toStatus
    ) {
        List<ConsultationStatus> allowed = fromStatus.getAllowedNextStatuses();
        boolean canTransition = fromStatus.canTransitionTo(toStatus);
        
        // canTransitionTo应该与getAllowedNextStatuses一致
        assert canTransition == allowed.contains(toStatus) :
            "canTransitionTo与getAllowedNextStatuses不一致";
    }
    
    /**
     * Property 7: fromCode方法正确解析状态码
     */
    @Property(tries = 100)
    @Label("fromCode正确解析状态码")
    void fromCodeCorrectlyParsesStatusCode(
            @ForAll("allStatuses") ConsultationStatus status
    ) {
        String code = status.getCode();
        ConsultationStatus parsed = ConsultationStatus.fromCode(code);
        
        assert parsed == status : 
            "fromCode(" + code + ")应该返回" + status + "，实际返回" + parsed;
    }
    
    /**
     * Property 8: 无效状态码返回null
     */
    @Property(tries = 100)
    @Label("无效状态码返回null")
    void invalidCodeReturnsNull(
            @ForAll("invalidCodes") String invalidCode
    ) {
        ConsultationStatus parsed = ConsultationStatus.fromCode(invalidCode);
        assert parsed == null : "无效状态码应该返回null";
    }
    
    @Provide
    Arbitrary<ConsultationStatus> allStatuses() {
        return Arbitraries.of(ConsultationStatus.values());
    }
    
    @Provide
    Arbitrary<ConsultationStatus> finalStatuses() {
        return Arbitraries.of(ConsultationStatus.FINISHED, ConsultationStatus.CANCELED);
    }
    
    @Provide
    Arbitrary<ConsultationStatus> nonFinalStatuses() {
        return Arbitraries.of(ConsultationStatus.WAITING, ConsultationStatus.IN_PROGRESS);
    }
    
    @Provide
    Arbitrary<String> invalidCodes() {
        return Arbitraries.of(
            "INVALID", "UNKNOWN", "PENDING", "COMPLETED", 
            "waiting", "in_progress", "", "null"
        );
    }
}
