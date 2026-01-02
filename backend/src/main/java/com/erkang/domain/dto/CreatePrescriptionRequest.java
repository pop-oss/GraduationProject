package com.erkang.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 创建处方请求DTO
 * _Requirements: 6.1_
 */
@Data
public class CreatePrescriptionRequest {
    private Long consultationId;
    private List<String> diagnosis;
    private List<PrescriptionItemDTO> items;

    @Data
    public static class PrescriptionItemDTO {
        private String drugName;
        private String spec;
        private String usage;
        private Integer quantity;
        private String unit;
        private String remark;
    }
}
