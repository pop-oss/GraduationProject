package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方Mapper
 */
@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
}
