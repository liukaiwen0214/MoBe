package com.mobe.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}