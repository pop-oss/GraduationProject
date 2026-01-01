package com.erkang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.domain.entity.*;
import com.erkang.domain.vo.DoctorVO;
import com.erkang.domain.vo.TimeSlotVO;
import com.erkang.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {
    
    private final DoctorProfileMapper doctorProfileMapper;
    private final UserMapper userMapper;
    private final HospitalMapper hospitalMapper;
    private final DepartmentMapper departmentMapper;
    private final AppointmentMapper appointmentMapper;
    
    /**
     * 分页查询医生列表
     */
    public Page<DoctorVO> listDoctors(int current, int size, Long departmentId, Boolean isExpert) {
        LambdaQueryWrapper<DoctorProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorProfile::getStatus, 1); // 只查询接诊中的医生
        
        if (departmentId != null) {
            wrapper.eq(DoctorProfile::getDepartmentId, departmentId);
        }
        if (isExpert != null) {
            wrapper.eq(DoctorProfile::getIsExpert, isExpert ? 1 : 0);
        }
        
        Page<DoctorProfile> page = doctorProfileMapper.selectPage(
                new Page<>(current, size), wrapper);
        
        Page<DoctorVO> result = new Page<>(current, size, page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList()));
        
        return result;
    }
    
    /**
     * 获取医生详情
     */
    public DoctorVO getDoctorById(Long doctorId) {
        DoctorProfile profile = doctorProfileMapper.selectById(doctorId);
        if (profile == null) {
            return null;
        }
        return toVO(profile);
    }
    
    /**
     * 根据用户ID获取医生信息
     */
    public DoctorVO getDoctorByUserId(Long userId) {
        DoctorProfile profile = doctorProfileMapper.selectByUserId(userId);
        if (profile == null) {
            return null;
        }
        return toVO(profile);
    }
    
    /**
     * 获取科室下的医生列表
     */
    public List<DoctorVO> listDoctorsByDepartment(Long departmentId) {
        LambdaQueryWrapper<DoctorProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorProfile::getDepartmentId, departmentId)
               .eq(DoctorProfile::getStatus, 1);
        
        return doctorProfileMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
    
    private DoctorVO toVO(DoctorProfile profile) {
        DoctorVO vo = new DoctorVO();
        vo.setId(profile.getId());
        vo.setUserId(profile.getUserId());
        vo.setTitle(profile.getTitle());
        vo.setSpecialty(profile.getSpecialty());
        vo.setIntroduction(profile.getIntroduction());
        vo.setConsultationFee(profile.getConsultationFee());
        vo.setIsExpert(profile.getIsExpert() == 1);
        vo.setStatus(profile.getStatus());
        
        // 获取用户信息
        User user = userMapper.selectById(profile.getUserId());
        if (user != null) {
            vo.setRealName(user.getRealName());
            vo.setAvatar(user.getAvatar());
        }
        
        // 获取医院信息
        if (profile.getHospitalId() != null) {
            Hospital hospital = hospitalMapper.selectById(profile.getHospitalId());
            if (hospital != null) {
                vo.setHospitalName(hospital.getName());
            }
        }
        
        // 获取科室信息
        if (profile.getDepartmentId() != null) {
            Department dept = departmentMapper.selectById(profile.getDepartmentId());
            if (dept != null) {
                vo.setDepartmentName(dept.getName());
            }
        }
        
        return vo;
    }
    
    /**
     * 获取医生指定日期的排班时间段
     * @param doctorId 医生ID
     * @param date 日期
     * @return 时间段列表
     */
    public List<TimeSlotVO> getDoctorSchedule(Long doctorId, LocalDate date) {
        List<TimeSlotVO> slots = new ArrayList<>();
        
        // 生成上午和下午的时间段 (9:00-12:00, 14:00-17:00)
        int slotId = 1;
        
        // 上午时段
        for (int hour = 9; hour < 12; hour++) {
            slots.add(createTimeSlot(slotId++, hour, 0, doctorId, date));
            slots.add(createTimeSlot(slotId++, hour, 30, doctorId, date));
        }
        
        // 下午时段
        for (int hour = 14; hour < 17; hour++) {
            slots.add(createTimeSlot(slotId++, hour, 0, doctorId, date));
            slots.add(createTimeSlot(slotId++, hour, 30, doctorId, date));
        }
        
        return slots;
    }
    
    private TimeSlotVO createTimeSlot(int id, int hour, int minute, Long doctorId, LocalDate date) {
        String startTime = String.format("%02d:%02d", hour, minute);
        String endTime = minute == 0 
            ? String.format("%02d:30", hour) 
            : String.format("%02d:00", hour + 1);
        
        // 检查该时间段是否已被预约
        boolean available = isSlotAvailable(doctorId, date, hour, minute);
        
        return TimeSlotVO.builder()
                .id(id)
                .startTime(startTime)
                .endTime(endTime)
                .available(available)
                .build();
    }
    
    /**
     * 检查时间段是否可用
     */
    private boolean isSlotAvailable(Long doctorId, LocalDate date, int hour, int minute) {
        // 如果是过去的时间，不可预约
        LocalDateTime slotTime = date.atTime(hour, minute);
        if (slotTime.isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // 查询该时间段是否已有预约
        String timeSlot = String.format("%02d:%02d", hour, minute);
        
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getDoctorId, doctorId)
               .eq(Appointment::getAppointmentDate, date)
               .eq(Appointment::getTimeSlot, timeSlot)
               .in(Appointment::getStatus, "PENDING", "CONFIRMED");
        
        Long count = appointmentMapper.selectCount(wrapper);
        return count == 0;
    }
}
