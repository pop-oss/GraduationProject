package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 病历Mapper
 */
@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {
}
