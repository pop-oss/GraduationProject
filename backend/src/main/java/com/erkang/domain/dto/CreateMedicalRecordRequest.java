package com.erkang.domain.dto;

import lombok.Data;

/**
 * 创建病历请求DTO
 * _Requirements: 5.1_
 */
@Data
public class CreateMedicalRecordRequest {
    private Long consultationId;
    private String chiefComplaint;
    private String presentIllness;
    private String pastHistory;
    private String allergies;
    private String physicalExam;
    private String diagnosis;
    private String treatment;
    private String advice;
}
