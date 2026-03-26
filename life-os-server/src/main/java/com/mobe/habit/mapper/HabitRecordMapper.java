package com.mobe.habit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.habit.entity.HabitRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 习惯执行记录 Mapper
 */
@Mapper
public interface HabitRecordMapper extends BaseMapper<HabitRecordEntity> {
}