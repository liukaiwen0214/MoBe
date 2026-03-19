package com.mobe.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 验证码实体类
 * <p>
 * 功能：存储验证码信息，用于用户注册、密码重置等场景
 * 说明：继承自 BaseEntity，使用 @TableName 注解指定数据库表名
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("auth_codes")
public class AuthCodeEntity extends BaseEntity {

    /**
     * 目标
     * <p>
     * 说明：验证码发送的目标，如邮箱地址或手机号
     */
    private String target;

    /**
     * 验证码
     * <p>
     * 说明：生成的验证码字符串
     */
    private String code;

    /**
     * 类型
     * <p>
     * 说明：验证码的类型，如 register、reset_password 等
     */
    private String type;

    /**
     * 过期时间
     * <p>
     * 说明：验证码的过期时间
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /**
     * 是否使用
     * <p>
     * 说明：0 表示未使用，1 表示已使用
     */
    private Integer used;

    /**
     * 使用时间
     * <p>
     * 说明：验证码被使用的时间
     */
    @TableField("used_at")
    private LocalDateTime usedAt;

    /**
     * 状态
     * <p>
     * 说明：验证码的状态，如 active、expired 等
     */
    private String status;
}