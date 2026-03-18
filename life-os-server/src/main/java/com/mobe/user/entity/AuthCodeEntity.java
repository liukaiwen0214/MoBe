package com.mobe.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("auth_codes")
public class AuthCodeEntity extends BaseEntity {

    private String target;

    private String code;

    private String type;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    private Integer used;

    @TableField("used_at")
    private LocalDateTime usedAt;

    private String status;
}