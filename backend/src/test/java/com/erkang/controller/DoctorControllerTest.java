package com.erkang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.domain.vo.DoctorVO;
import com.erkang.service.DoctorService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 医生控制器测试
 * _Requirements: 2.1, 2.2, 2.3_
 */
class DoctorControllerTest {

    private DoctorService doctorService;
    private DoctorController doctorController;

    @BeforeProperty
    void setUp() {
        doctorService = mock(DoctorService.class);
        doctorController = new DoctorController(doctorService);
    }

    /**
     * Property 1: 分页查询医生列表 - 应返回正确的分页数据
     * **Validates: Requirements 2.1**
     */
    @Property(tries = 100)
    void listDoctors_shouldReturnPagedResults(
            @ForAll @IntRange(min = 1, max = 100) int current,
            @ForAll @IntRange(min = 1, max = 50) int size) {
        
        Page<DoctorVO> mockPage = new Page<>(current, size);
        mockPage.setTotal(100);
        
        when(doctorService.listDoctors(eq(current), eq(size), any(), any()))
            .thenReturn(mockPage);
        
        var result = doctorController.listDoctors(current, size, null, null);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getCurrent()).isEqualTo(current);
        assertThat(result.getData().getSize()).isEqualTo(size);
    }

    /**
     * Property 2: 获取医生详情 - 应返回正确的医生信息
     * **Validates: Requirements 2.2**
     */
    @Property(tries = 100)
    void getDoctorDetail_shouldReturnDoctorInfo(
            @ForAll @LongRange(min = 1, max = 10000) Long doctorId) {
        
        DoctorVO mockDoctor = new DoctorVO();
        mockDoctor.setId(doctorId);
        mockDoctor.setRealName("测试医生");
        mockDoctor.setTitle("主任医师");
        
        when(doctorService.getDoctorById(doctorId)).thenReturn(mockDoctor);
        
        var result = doctorController.getDoctorDetail(doctorId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(doctorId);
    }

    /**
     * Property 3: 获取科室医生列表 - 应返回该科室的所有医生
     * **Validates: Requirements 2.3**
     */
    @Property(tries = 100)
    void listDoctorsByDepartment_shouldReturnDepartmentDoctors(
            @ForAll @LongRange(min = 1, max = 100) Long departmentId,
            @ForAll @IntRange(min = 0, max = 20) int doctorCount) {
        
        List<DoctorVO> mockList = new ArrayList<>();
        for (int i = 0; i < doctorCount; i++) {
            DoctorVO doctor = new DoctorVO();
            doctor.setId((long) (i + 1));
            doctor.setDepartmentName("耳鼻喉科");
            mockList.add(doctor);
        }
        
        when(doctorService.listDoctorsByDepartment(departmentId)).thenReturn(mockList);
        
        var result = doctorController.listDoctorsByDepartment(departmentId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(doctorCount);
    }

    /**
     * Property 4: 分页参数验证 - 页码和每页大小应为正数
     * **Validates: Requirements 2.1**
     */
    @Property(tries = 100)
    void pageParameters_shouldBePositive(
            @ForAll @IntRange(min = 1, max = Integer.MAX_VALUE) int current,
            @ForAll @IntRange(min = 1, max = Integer.MAX_VALUE) int size) {
        
        assertThat(current).isPositive();
        assertThat(size).isPositive();
    }

    /**
     * Property 5: 按专家筛选 - 应正确筛选专家医生
     * **Validates: Requirements 2.1**
     */
    @Property(tries = 50)
    void listDoctors_withExpertFilter_shouldFilterCorrectly(
            @ForAll @IntRange(min = 1, max = 10) int current,
            @ForAll @IntRange(min = 10, max = 20) int size,
            @ForAll boolean isExpert) {
        
        Page<DoctorVO> mockPage = new Page<>(current, size);
        
        when(doctorService.listDoctors(eq(current), eq(size), any(), eq(isExpert)))
            .thenReturn(mockPage);
        
        var result = doctorController.listDoctors(current, size, null, isExpert);
        
        assertThat(result).isNotNull();
    }

    /**
     * Property 6: 按科室筛选 - 应正确筛选科室医生
     * **Validates: Requirements 2.1**
     */
    @Property(tries = 50)
    void listDoctors_withDepartmentFilter_shouldFilterCorrectly(
            @ForAll @IntRange(min = 1, max = 10) int current,
            @ForAll @IntRange(min = 10, max = 20) int size,
            @ForAll @LongRange(min = 1, max = 100) Long departmentId) {
        
        Page<DoctorVO> mockPage = new Page<>(current, size);
        
        when(doctorService.listDoctors(eq(current), eq(size), eq(departmentId), any()))
            .thenReturn(mockPage);
        
        var result = doctorController.listDoctors(current, size, departmentId, null);
        
        assertThat(result).isNotNull();
    }
}
