package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科室Mapper
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
