package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.Referral;
import org.apache.ibatis.annotations.Mapper;

/**
 * 转诊Mapper
 */
@Mapper
public interface ReferralMapper extends BaseMapper<Referral> {
}
