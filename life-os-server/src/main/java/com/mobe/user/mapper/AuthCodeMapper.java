package com.mobe.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobe.user.entity.AuthCodeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthCodeMapper extends BaseMapper<AuthCodeEntity> {
}