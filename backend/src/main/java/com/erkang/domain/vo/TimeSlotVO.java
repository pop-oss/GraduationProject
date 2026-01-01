package com.erkang.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生排班时间段 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotVO {
    
    /**
     * 时间段ID
     */
    private Integer id;
    
    /**
     * 开始时间 (HH:mm)
     */
    private String startTime;
    
    /**
     * 结束时间 (HH:mm)
     */
    private String endTime;
    
    /**
     * 是否可预约
     */
    private Boolean available;
}
