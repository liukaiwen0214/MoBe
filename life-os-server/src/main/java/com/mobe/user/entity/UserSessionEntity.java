package com.mobe.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_session")
public class UserSessionEntity extends UserOwnedEntity {

    @TableField("session_token")
    private String sessionToken;

    @TableField("remember_me")
    private Integer rememberMe;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @TableField("device_type")
    private String deviceType;

    @TableField("client_ip")
    private String clientIp;

    @TableField("user_agent")
    private String userAgent;

    private String status;
}