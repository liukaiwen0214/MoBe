package com.mobe.checklist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.checklist.entity.ChecklistInstanceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 清单展示实例 Mapper
 */
@Mapper
public interface ChecklistInstanceMapper extends BaseMapper<ChecklistInstanceEntity> {
}