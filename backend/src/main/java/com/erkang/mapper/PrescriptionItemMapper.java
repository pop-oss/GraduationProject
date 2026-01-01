package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.PrescriptionItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方明细Mapper
 */
@Mapper
public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItem> {
}
