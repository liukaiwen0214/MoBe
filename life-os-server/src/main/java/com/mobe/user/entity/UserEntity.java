package com.mobe.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 * <p>
 * 功能：存储用户的基本信息
 * 说明：继承自 BaseEntity，使用 @TableName 注解指定数据库表名
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class UserEntity extends BaseEntity {

    /**
     * 用户名
     * <p>
     * 说明：用户登录时使用的用户名
     */
    private String username;

    /**
     * 邮箱
     * <p>
     * 说明：用户的邮箱地址，用于登录和找回密码
     */
    private String email;

    /**
     * 密码哈希
     * <p>
     * 说明：使用 BCrypt 加密后的密码哈希值
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 昵称
     * <p>
     * 说明：用户的显示名称
     */
    private String nickname;

    /**
     * 头像URL
     * <p>
     * 说明：用户头像的网络地址
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 状态
     * <p>
     * 说明：用户的状态，如 active、inactive 等
     */
    private String status;
}