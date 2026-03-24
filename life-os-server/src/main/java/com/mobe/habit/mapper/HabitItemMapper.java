package com.mobe.habit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.habit.entity.HabitItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 习惯定义 Mapper
 */
@Mapper
public interface HabitItemMapper extends BaseMapper<HabitItemEntity> {
}