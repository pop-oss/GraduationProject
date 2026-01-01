package com.erkang.domain.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 问诊状态枚举
 */
@Getter
public enum ConsultationStatus {
    
    WAITING("WAITING", "待接诊"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    FINISHED("FINISHED", "已结束"),
    CANCELED("CANCELED", "已取消");
    
    private final String code;
    private final String name;
    
    ConsultationStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 获取允许的下一状态列表
     */
    public List<ConsultationStatus> getAllowedNextStatuses() {
        return switch (this) {
            case WAITING -> Arrays.asList(IN_PROGRESS, CANCELED);
            case IN_PROGRESS -> Arrays.asList(FINISHED, CANCELED);
            case FINISHED, CANCELED -> List.of(); // 终态，不允许变更
        };
    }
    
    /**
     * 检查是否可以转换到目标状态
     */
    public boolean canTransitionTo(ConsultationStatus target) {
        return getAllowedNextStatuses().contains(target);
    }
    
    /**
     * 是否为终态
     */
    public boolean isFinal() {
        return this == FINISHED || this == CANCELED;
    }
    
    public static ConsultationStatus fromCode(String code) {
        for (ConsultationStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
