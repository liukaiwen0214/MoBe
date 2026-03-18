package com.mobe.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_preferences")
public class UserPreferencesEntity extends UserOwnedEntity {

    private String theme;

    private String language;

    @TableField("default_homepage")
    private String defaultHomepage;

    @TableField("date_format")
    private String dateFormat;

    @TableField("time_format")
    private String timeFormat;

    @TableField("week_start_day")
    private String weekStartDay;

    @TableField("notification_enabled")
    private Integer notificationEnabled;

    @TableField("email_notification_enabled")
    private Integer emailNotificationEnabled;
}