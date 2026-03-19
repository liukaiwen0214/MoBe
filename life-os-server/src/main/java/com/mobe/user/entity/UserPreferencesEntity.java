package com.mobe.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户偏好设置实体类
 * <p>
 * 功能：存储用户的偏好设置信息
 * 说明：继承自 UserOwnedEntity，使用 @TableName 注解指定数据库表名
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_preferences")
public class UserPreferencesEntity extends UserOwnedEntity {

    /**
     * 主题
     * <p>
     * 说明：用户选择的界面主题，如 light、dark 等
     */
    private String theme;

    /**
     * 语言
     * <p>
     * 说明：用户选择的语言，如 zh-CN、en-US 等
     */
    private String language;

    /**
     * 默认首页
     * <p>
     * 说明：用户设置的默认首页路径
     */
    @TableField("default_homepage")
    private String defaultHomepage;

    /**
     * 日期格式
     * <p>
     * 说明：用户选择的日期显示格式
     */
    @TableField("date_format")
    private String dateFormat;

    /**
     * 时间格式
     * <p>
     * 说明：用户选择的时间显示格式
     */
    @TableField("time_format")
    private String timeFormat;

    /**
     * 周开始日
     * <p>
     * 说明：用户选择的一周开始日，如 Monday、Sunday 等
     */
    @TableField("week_start_day")
    private String weekStartDay;

    /**
     * 是否启用通知
     * <p>
     * 说明：0 表示禁用，1 表示启用
     */
    @TableField("notification_enabled")
    private Integer notificationEnabled;

    /**
     * 是否启用邮件通知
     * <p>
     * 说明：0 表示禁用，1 表示启用
     */
    @TableField("email_notification_enabled")
    private Integer emailNotificationEnabled;
}