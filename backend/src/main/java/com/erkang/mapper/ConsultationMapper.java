package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.domain.dto.ConsultationListDTO;
import com.erkang.domain.entity.Consultation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 问诊Mapper
 */
@Mapper
public interface ConsultationMapper extends BaseMapper<Consultation> {
    
    /**
     * 分页查询问诊列表（包含患者信息）
     */
    @Select("SELECT c.id, c.consultation_no, c.patient_id, c.doctor_id, " +
            "c.consultation_type, c.status, c.symptoms, c.scheduled_at, " +
            "c.start_time, c.end_time, c.duration, c.created_at, " +
            "u.real_name as patient_name, u.phone as patient_phone " +
            "FROM consultation c " +
            "LEFT JOIN sys_user u ON c.patient_id = u.id " +
            "WHERE c.doctor_id = #{doctorId} AND c.status = #{status} " +
            "ORDER BY c.created_at ASC")
    Page<ConsultationListDTO> selectConsultationListByStatus(
            Page<ConsultationListDTO> page,
            @Param("doctorId") Long doctorId,
            @Param("status") String status);
    
    /**
     * 分页查询进行中问诊列表（包含患者信息）
     */
    @Select("SELECT c.id, c.consultation_no, c.patient_id, c.doctor_id, " +
            "c.consultation_type, c.status, c.symptoms, c.scheduled_at, " +
            "c.start_time, c.end_time, c.duration, c.created_at, " +
            "u.real_name as patient_name, u.phone as patient_phone " +
            "FROM consultation c " +
            "LEFT JOIN sys_user u ON c.patient_id = u.id " +
            "WHERE c.doctor_id = #{doctorId} AND c.status = 'IN_PROGRESS' " +
            "ORDER BY c.start_time DESC")
    Page<ConsultationListDTO> selectInProgressList(
            Page<ConsultationListDTO> page,
            @Param("doctorId") Long doctorId);
    
    /**
     * 查询问诊详情（包含患者信息）
     */
    @Select("SELECT c.id, c.consultation_no as consultationNo, c.patient_id as patientId, c.doctor_id as doctorId, " +
            "c.consultation_type as consultationType, c.status, c.symptoms, c.scheduled_at as scheduledAt, " +
            "c.start_time as startedAt, c.end_time as finishedAt, c.duration, c.rtc_room_id as rtcRoomId, c.created_at as createdAt, " +
            "u.id as patient_user_id, u.real_name as patient_name, u.phone as patient_phone, " +
            "pp.gender as patient_gender, pp.birth_date as patient_birth_date " +
            "FROM consultation c " +
            "LEFT JOIN sys_user u ON c.patient_id = u.id " +
            "LEFT JOIN patient_profile pp ON c.patient_id = pp.user_id " +
            "WHERE c.id = #{consultationId}")
    Map<String, Object> selectDetailById(@Param("consultationId") Long consultationId);
}
