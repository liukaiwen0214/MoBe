package com.mobe.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.user.entity.UserSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSessionMapper extends BaseMapper<UserSessionEntity> {
}