package com.mobe.checklist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.checklist.entity.ChecklistItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 普通清单 Mapper
 */
@Mapper
public interface ChecklistItemMapper extends BaseMapper<ChecklistItemEntity> {
}