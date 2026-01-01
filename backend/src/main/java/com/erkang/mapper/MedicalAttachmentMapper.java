package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.MedicalAttachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 病历附件Mapper
 */
@Mapper
public interface MedicalAttachmentMapper extends BaseMapper<MedicalAttachment> {
}
