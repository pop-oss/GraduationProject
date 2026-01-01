package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.AIChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AIChatMessageMapper extends BaseMapper<AIChatMessage> {
}
