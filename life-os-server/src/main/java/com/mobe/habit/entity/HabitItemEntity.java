package com.mobe.habit.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 习惯定义实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("habit_item")
public class HabitItemEntity extends UserOwnedEntity {

    /**
     * 所属任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 所属任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 习惯标题
     */
    @TableField("title")
    private String title;

    /**
     * 习惯说明
     */
    @TableField("description")
    private String description;

    /**
     * 优先级：HIGH/MEDIUM/LOW
     */
    @TableField("priority")
    private String priority;

    /**
     * 频率类型：DAILY/WEEKLY/MONTHLY
     */
    @TableField("frequency_type")
    private String frequencyType;

    /**
     * 频率配置，如周几/月几号
     */
    @TableField("frequency_value")
    private String frequencyValue;

    /**
     * 提醒文案
     */
    @TableField("reminder_text")
    private String reminderText;

    /**
     * 动作文案
     */
    @TableField("action_text")
    private String actionText;

    /**
     * 动作类型
     */
    @TableField("action_type")
    private String actionType;

    /**
     * 是否启用：1启用 0停用
     */
    @TableField("is_enabled")
    private Integer isEnabled;

    /**
     * 排序值
     */
    @TableField("sort")
    private Integer sort;
}