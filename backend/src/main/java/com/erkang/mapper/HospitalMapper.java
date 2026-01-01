package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.Hospital;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医院Mapper
 */
@Mapper
public interface HospitalMapper extends BaseMapper<Hospital> {
}
