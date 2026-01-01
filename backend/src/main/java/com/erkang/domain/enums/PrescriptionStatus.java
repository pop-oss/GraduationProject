package com.erkang.domain.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 处方状态枚举
 * _Requirements: 6.2_
 */
@Getter
public enum PrescriptionStatus {
    
    DRAFT("DRAFT", "草稿"),
    PENDING_REVIEW("PENDING_REVIEW", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回"),
    DISPENSED("DISPENSED", "已发药");
    
    private final String code;
    private final String name;
    
    PrescriptionStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 获取允许的下一状态列表
     */
    public List<PrescriptionStatus> getAllowedNextStatuses() {
        return switch (this) {
            case DRAFT -> List.of(PENDING_REVIEW);
            case PENDING_REVIEW -> Arrays.asList(APPROVED, REJECTED);
            case APPROVED -> List.of(DISPENSED);
            case REJECTED -> List.of(DRAFT); // 驳回后可重新编辑
            case DISPENSED -> List.of(); // 终态
        };
    }
    
    /**
     * 检查是否可以转换到目标状态
     */
    public boolean canTransitionTo(PrescriptionStatus target) {
        return getAllowedNextStatuses().contains(target);
    }
    
    /**
     * 是否为终态
     */
    public boolean isFinal() {
        return this == DISPENSED;
    }
    
    /**
     * 是否可编辑
     */
    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }
    
    public static PrescriptionStatus fromCode(String code) {
        for (PrescriptionStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
