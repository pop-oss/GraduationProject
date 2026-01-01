package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.Consultation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问诊Mapper
 */
@Mapper
public interface ConsultationMapper extends BaseMapper<Consultation> {
}
