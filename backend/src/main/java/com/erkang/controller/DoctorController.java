package com.erkang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.common.Result;
import com.erkang.domain.vo.DoctorVO;
import com.erkang.domain.vo.TimeSlotVO;
import com.erkang.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生控制器
 */
@Tag(name = "医生管理", description = "医生信息查询")
@RestController
@RequiredArgsConstructor
public class DoctorController {
    
    private final DoctorService doctorService;
    
    @Operation(summary = "分页查询医生列表")
    @GetMapping({"/api/doctor/list", "/api/doctors"})
    public Result<Page<DoctorVO>> listDoctors(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean isExpert) {
        return Result.success(doctorService.listDoctors(current, size, departmentId, isExpert));
    }
    
    @Operation(summary = "获取专家医生列表")
    @GetMapping("/api/doctors/experts")
    public Result<Page<DoctorVO>> listExperts(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "100") int size) {
        return Result.success(doctorService.listDoctors(current, size, null, true));
    }
    
    @Operation(summary = "获取医生详情")
    @GetMapping({"/api/doctor/{doctorId}", "/api/doctors/{doctorId}"})
    public Result<DoctorVO> getDoctorDetail(@PathVariable Long doctorId) {
        return Result.success(doctorService.getDoctorById(doctorId));
    }
    
    @Operation(summary = "获取科室医生列表")
    @GetMapping("/api/doctor/department/{departmentId}")
    public Result<List<DoctorVO>> listDoctorsByDepartment(@PathVariable Long departmentId) {
        return Result.success(doctorService.listDoctorsByDepartment(departmentId));
    }
    
    @Operation(summary = "获取医生排班时间段")
    @GetMapping({"/api/doctor/{doctorId}/schedule", "/api/doctors/{doctorId}/schedule"})
    public Result<List<TimeSlotVO>> getDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(doctorService.getDoctorSchedule(doctorId, date));
    }
}
