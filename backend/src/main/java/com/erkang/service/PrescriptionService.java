package com.erkang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.domain.dto.CreatePrescriptionRequest;
import com.erkang.domain.dto.PrescriptionReviewDTO;
import com.erkang.domain.entity.*;
import com.erkang.domain.enums.PrescriptionStatus;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.PatientProfileMapper;
import com.erkang.mapper.PrescriptionItemMapper;
import com.erkang.mapper.PrescriptionMapper;
import com.erkang.security.Auditable;
import com.erkang.security.DataScope;
import com.erkang.security.DataScopeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 处方服务
 * _Requirements: 6.1, 6.2, 6.7_
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper itemMapper;
    private final ConsultationMapper consultationMapper;
    private final PatientProfileMapper patientProfileMapper;

    /**
     * 创建处方并添加明细（一次性提交）
     */
    @Transactional
    @Auditable(action = "CREATE_PRESCRIPTION", module = "prescription")
    public Prescription createPrescriptionWithItems(CreatePrescriptionRequest request, Long doctorId) {
        Long consultationId = request.getConsultationId();
        
        // 获取问诊信息以获取patientId
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "问诊不存在");
        }
        Long patientId = consultation.getPatientId();
        
        // 创建处方
        Prescription prescription = new Prescription();
        prescription.setConsultationId(consultationId);
        prescription.setPatientId(patientId);
        prescription.setDoctorId(doctorId);
        prescription.setPrescriptionNo(generatePrescriptionNo());
        prescription.setStatus(PrescriptionStatus.PENDING_REVIEW.getCode()); // 直接提交审核
        if (request.getDiagnosis() != null && !request.getDiagnosis().isEmpty()) {
            prescription.setDiagnosis(String.join("; ", request.getDiagnosis()));
        }
        prescription.setSubmittedAt(LocalDateTime.now());
        prescription.setCreatedAt(LocalDateTime.now());
        prescription.setUpdatedAt(LocalDateTime.now());
        
        prescriptionMapper.insert(prescription);
        
        // 添加处方明细
        if (request.getItems() != null) {
            for (CreatePrescriptionRequest.PrescriptionItemDTO itemDTO : request.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setPrescriptionId(prescription.getId());
                item.setDrugName(itemDTO.getDrugName());
                item.setDrugSpec(itemDTO.getSpec());
                item.setDosage(itemDTO.getUsage());
                item.setQuantity(itemDTO.getQuantity());
                item.setUnit(itemDTO.getUnit());
                item.setNotes(itemDTO.getRemark());
                item.setCreatedAt(LocalDateTime.now());
                itemMapper.insert(item);
            }
        }
        
        log.info("创建处方: prescriptionNo={}, consultationId={}, items={}", 
                prescription.getPrescriptionNo(), consultationId, 
                request.getItems() != null ? request.getItems().size() : 0);
        return prescription;
    }

    /**
     * 创建处方
     */
    @Transactional
    @Auditable(action = "CREATE_PRESCRIPTION", module = "prescription")
    public Prescription createPrescription(Long consultationId, Long patientId, Long doctorId) {
        Prescription prescription = new Prescription();
        prescription.setConsultationId(consultationId);
        prescription.setPatientId(patientId);
        prescription.setDoctorId(doctorId);
        prescription.setPrescriptionNo(generatePrescriptionNo());
        prescription.setStatus(PrescriptionStatus.DRAFT.getCode());
        prescription.setCreatedAt(LocalDateTime.now());
        prescription.setUpdatedAt(LocalDateTime.now());
        
        prescriptionMapper.insert(prescription);
        log.info("创建处方: prescriptionNo={}, consultationId={}", prescription.getPrescriptionNo(), consultationId);
        return prescription;
    }

    /**
     * 更新处方
     */
    @Transactional
    @Auditable(action = "UPDATE_PRESCRIPTION", module = "prescription")
    public Prescription updatePrescription(Long prescriptionId, Prescription updates) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        
        PrescriptionStatus status = PrescriptionStatus.fromCode(prescription.getStatus());
        if (status == null || !status.isEditable()) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_STATUS_INVALID, "当前状态不允许修改");
        }
        
        if (updates.getDiagnosis() != null) prescription.setDiagnosis(updates.getDiagnosis());
        if (updates.getNotes() != null) prescription.setNotes(updates.getNotes());
        prescription.setUpdatedAt(LocalDateTime.now());
        
        prescriptionMapper.updateById(prescription);
        return prescription;
    }

    /**
     * 添加处方明细
     */
    @Transactional
    @Auditable(action = "ADD_PRESCRIPTION_ITEM", module = "prescription")
    public PrescriptionItem addItem(Long prescriptionId, PrescriptionItem item) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        
        PrescriptionStatus status = PrescriptionStatus.fromCode(prescription.getStatus());
        if (status == null || !status.isEditable()) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_STATUS_INVALID, "当前状态不允许添加药品");
        }
        
        item.setPrescriptionId(prescriptionId);
        item.setCreatedAt(LocalDateTime.now());
        itemMapper.insert(item);
        return item;
    }

    /**
     * 删除处方明细
     */
    @Transactional
    @Auditable(action = "DELETE_PRESCRIPTION_ITEM", module = "prescription")
    public void deleteItem(Long prescriptionId, Long itemId) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        
        PrescriptionStatus status = PrescriptionStatus.fromCode(prescription.getStatus());
        if (status == null || !status.isEditable()) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_STATUS_INVALID, "当前状态不允许删除药品");
        }
        
        itemMapper.deleteById(itemId);
    }

    /**
     * 提交处方审核
     */
    @Transactional
    @Auditable(action = "SUBMIT_PRESCRIPTION", module = "prescription")
    public Prescription submitForReview(Long prescriptionId) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        
        PrescriptionStatus currentStatus = PrescriptionStatus.fromCode(prescription.getStatus());
        PrescriptionStatus targetStatus = PrescriptionStatus.PENDING_REVIEW;
        
        if (currentStatus == null || !currentStatus.canTransitionTo(targetStatus)) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_STATUS_INVALID, 
                    "状态流转不合法: " + currentStatus + " -> " + targetStatus);
        }
        
        // 校验处方明细
        List<PrescriptionItem> items = listItems(prescriptionId);
        if (items.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "处方明细不能为空");
        }
        
        prescription.setStatus(targetStatus.getCode());
        prescription.setSubmittedAt(LocalDateTime.now());
        prescription.setUpdatedAt(LocalDateTime.now());
        prescriptionMapper.updateById(prescription);
        
        log.info("处方提交审核: prescriptionNo={}", prescription.getPrescriptionNo());
        return prescription;
    }

    /**
     * 审核通过
     */
    @Transactional
    @Auditable(action = "APPROVE_PRESCRIPTION", module = "prescription")
    public Prescription approve(Long prescriptionId) {
        return transitionStatus(prescriptionId, PrescriptionStatus.APPROVED);
    }

    /**
     * 审核驳回
     */
    @Transactional
    @Auditable(action = "REJECT_PRESCRIPTION", module = "prescription")
    public Prescription reject(Long prescriptionId, String reason) {
        Prescription prescription = transitionStatus(prescriptionId, PrescriptionStatus.REJECTED);
        prescription.setNotes(reason);
        prescriptionMapper.updateById(prescription);
        return prescription;
    }

    /**
     * 发药
     */
    @Transactional
    @Auditable(action = "DISPENSE_PRESCRIPTION", module = "prescription")
    public Prescription dispense(Long prescriptionId) {
        Prescription prescription = transitionStatus(prescriptionId, PrescriptionStatus.DISPENSED);
        prescription.setStatusUpdatedAt(LocalDateTime.now());
        prescriptionMapper.updateById(prescription);
        return prescription;
    }

    /**
     * 状态流转
     */
    private Prescription transitionStatus(Long prescriptionId, PrescriptionStatus targetStatus) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        
        PrescriptionStatus currentStatus = PrescriptionStatus.fromCode(prescription.getStatus());
        if (currentStatus == null || !currentStatus.canTransitionTo(targetStatus)) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_STATUS_INVALID, 
                    "状态流转不合法: " + currentStatus + " -> " + targetStatus);
        }
        
        prescription.setStatus(targetStatus.getCode());
        prescription.setStatusUpdatedAt(LocalDateTime.now());
        if (targetStatus == PrescriptionStatus.APPROVED) {
            prescription.setApprovedAt(LocalDateTime.now());
        }
        prescription.setUpdatedAt(LocalDateTime.now());
        prescriptionMapper.updateById(prescription);
        
        log.info("处方状态变更: prescriptionNo={}, {} -> {}", 
                prescription.getPrescriptionNo(), currentStatus, targetStatus);
        return prescription;
    }

    /**
     * 查询处方详情
     */
    public Prescription getById(Long prescriptionId) {
        return prescriptionMapper.selectById(prescriptionId);
    }

    /**
     * 查询处方明细
     */
    public List<PrescriptionItem> listItems(Long prescriptionId) {
        LambdaQueryWrapper<PrescriptionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrescriptionItem::getPrescriptionId, prescriptionId)
               .orderByAsc(PrescriptionItem::getId);
        return itemMapper.selectList(wrapper);
    }

    /**
     * 查询患者处方列表
     */
    @DataScope(value = DataScopeType.PATIENT_SELF)
    public List<Prescription> listByPatientId(Long patientId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getPatientId, patientId)
               .orderByDesc(Prescription::getCreatedAt);
        return prescriptionMapper.selectList(wrapper);
    }

    /**
     * 查询待审核处方列表
     */
    public List<Prescription> listPendingReview() {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getStatus, PrescriptionStatus.PENDING_REVIEW.getCode())
               .orderByAsc(Prescription::getSubmittedAt);
        return prescriptionMapper.selectList(wrapper);
    }

    /**
     * 分页查询待审核处方列表
     */
    public Page<Prescription> listPendingReviewPage(int page, int size) {
        Page<Prescription> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getStatus, PrescriptionStatus.PENDING_REVIEW.getCode())
               .orderByAsc(Prescription::getSubmittedAt);
        return prescriptionMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 分页查询待审核处方列表（包含患者姓名、医生姓名、药品数量）
     */
    public Page<PrescriptionReviewDTO> listPendingReviewDTOPage(int page, int size) {
        Page<PrescriptionReviewDTO> pageParam = new Page<>(page, size);
        return prescriptionMapper.selectPendingReviewPage(pageParam, PrescriptionStatus.PENDING_REVIEW.getCode());
    }

    /**
     * 查询处方详情（包含患者姓名、医生姓名、药品数量、患者信息、药品明细）
     */
    public PrescriptionReviewDTO getReviewDetailById(Long id) {
        PrescriptionReviewDTO dto = prescriptionMapper.selectReviewDetailById(id);
        if (dto == null) {
            return null;
        }
        
        // 填充患者信息（年龄、性别、过敏史）
        if (dto.getPatientId() != null) {
            PatientProfile profile = patientProfileMapper.selectByUserId(dto.getPatientId());
            if (profile != null) {
                // 计算年龄
                if (profile.getBirthDate() != null) {
                    dto.setPatientAge(Period.between(profile.getBirthDate(), LocalDate.now()).getYears());
                }
                // 性别
                if (profile.getGender() != null) {
                    dto.setPatientGender(profile.getGender() == 1 ? "男" : profile.getGender() == 0 ? "女" : "未知");
                }
                // 过敏史
                if (profile.getAllergyHistory() != null && !profile.getAllergyHistory().isEmpty() 
                        && !"无".equals(profile.getAllergyHistory())) {
                    dto.setAllergies(Arrays.asList(profile.getAllergyHistory().split("[,，、]")));
                } else {
                    dto.setAllergies(Collections.emptyList());
                }
            }
        }
        
        // 填充药品明细
        List<PrescriptionItem> items = listItems(id);
        dto.setItems(items);
        
        return dto;
    }

    /**
     * 统计待审核处方数量
     */
    public long countPendingReview() {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getStatus, PrescriptionStatus.PENDING_REVIEW.getCode());
        return prescriptionMapper.selectCount(wrapper);
    }

    /**
     * 根据问诊ID查询处方
     */
    public Prescription getByConsultationId(Long consultationId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getConsultationId, consultationId);
        return prescriptionMapper.selectOne(wrapper);
    }

    private String generatePrescriptionNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "RX" + date + uuid;
    }
    
    /**
     * 分页查询药师审方历史（包含处方详情）
     */
    public Page<PrescriptionReviewDTO> listReviewHistoryByPharmacist(Long pharmacistId, int page, int size) {
        return listReviewHistoryByPharmacist(pharmacistId, page, size, null, null, null);
    }
    
    /**
     * 分页查询药师审方历史（包含处方详情，支持筛选）
     */
    public Page<PrescriptionReviewDTO> listReviewHistoryByPharmacist(Long pharmacistId, int page, int size,
            String status, String startDate, String endDate) {
        Page<PrescriptionReviewDTO> pageParam = new Page<>(page, size);
        return prescriptionMapper.selectReviewHistoryByPharmacist(pageParam, pharmacistId, status, startDate, endDate);
    }
    
    /**
     * 分页查询所有审方历史（支持筛选，不限制药师）
     */
    public Page<PrescriptionReviewDTO> listAllReviewHistory(int page, int size,
            String status, String startDate, String endDate) {
        Page<PrescriptionReviewDTO> pageParam = new Page<>(page, size);
        return prescriptionMapper.selectAllReviewHistory(pageParam, status, startDate, endDate);
    }
}
