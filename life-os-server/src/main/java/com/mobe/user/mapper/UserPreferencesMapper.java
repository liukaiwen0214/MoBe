package com.mobe.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.user.entity.UserPreferencesEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户偏好设置Mapper
 * <p>
 * 功能：提供用户偏好设置相关的数据库操作方法
 * 说明：继承自 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作
 */
@Mapper
public interface UserPreferencesMapper extends BaseMapper<UserPreferencesEntity> {
}