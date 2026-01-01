package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.DoctorProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 医生档案Mapper
 */
@Mapper
public interface DoctorProfileMapper extends BaseMapper<DoctorProfile> {
    
    @Select("SELECT * FROM doctor_profile WHERE user_id = #{userId}")
    DoctorProfile selectByUserId(@Param("userId") Long userId);
}
