package com.mobe.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户会话实体类
 * <p>
 * 功能：存储用户的会话信息，用于用户登录状态管理
 * 说明：继承自 UserOwnedEntity，使用 @TableName 注解指定数据库表名
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_session")
public class UserSessionEntity extends UserOwnedEntity {

    /**
     * 会话令牌
     * <p>
     * 说明：用户登录后生成的会话令牌，用于身份验证
     */
    @TableField("session_token")
    private String sessionToken;

    /**
     * 是否记住我
     * <p>
     * 说明：0 表示不记住，1 表示记住
     */
    @TableField("remember_me")
    private Integer rememberMe;

    /**
     * 过期时间
     * <p>
     * 说明：会话的过期时间
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /**
     * 最后访问时间
     * <p>
     * 说明：用户最后访问系统的时间
     */
    @TableField("last_accessed_at")
    private LocalDateTime lastAccessedAt;

    /**
     * 设备类型
     * <p>
     * 说明：用户登录时使用的设备类型，如 mobile、desktop 等
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 客户端IP
     * <p>
     * 说明：用户登录时的 IP 地址
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 用户代理
     * <p>
     * 说明：用户登录时使用的浏览器或应用的用户代理字符串
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 状态
     * <p>
     * 说明：会话的状态，如 active、expired 等
     */
    private String status;
}