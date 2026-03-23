package com.mobe.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.finance.entity.FinanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 财务记录Mapper
 * <p>
 * 功能：提供财务记录相关的数据库操作方法
 * 说明：继承自 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作
 */
@Mapper
public interface FinanceRecordMapper extends BaseMapper<FinanceRecordEntity> {
}