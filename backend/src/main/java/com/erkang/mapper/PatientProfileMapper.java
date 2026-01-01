package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.PatientProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 患者档案Mapper
 */
@Mapper
public interface PatientProfileMapper extends BaseMapper<PatientProfile> {
    
    /**
     * 根据用户ID查询患者档案
     */
    @Select("SELECT * FROM patient_profile WHERE user_id = #{userId}")
    PatientProfile selectByUserId(@Param("userId") Long userId);
}
