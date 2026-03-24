package com.mobe.habit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.habit.entity.HabitCheckinRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 习惯打卡记录 Mapper
 */
@Mapper
public interface HabitCheckinRecordMapper extends BaseMapper<HabitCheckinRecordEntity> {
}