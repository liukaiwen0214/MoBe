package com.mobe.checklist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.checklist.entity.ChecklistExecutionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 清单执行 Mapper
 */
@Mapper
public interface ChecklistExecutionMapper extends BaseMapper<ChecklistExecutionEntity> {
}