package com.mobe.habit.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 习惯实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("habit")
public class HabitEntity extends UserOwnedEntity {

    /**
     * 所属任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 对应清单ID
     */
    @TableField("checklist_id")
    private String checklistId;

    /**
     * 习惯名称
     */
    @TableField("name")
    private String name;

    /**
     * 习惯描述
     */
    @TableField("description")
    private String description;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 颜色
     */
    @TableField("color")
    private String color;

    /**
     * 频率类型：DAILY / WEEKLY / MONTHLY
     */
    @TableField("frequency_type")
    private String frequencyType;

    /**
     * 频率值：如 EVERYDAY / 1,3,5 / 1,15,28
     */
    @TableField("frequency_value")
    private String frequencyValue;

    /**
     * 开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 提醒时间
     */
    @TableField("reminder_time")
    private LocalTime reminderTime;

    /**
     * 是否生成清单执行：1是 0否
     */
    @TableField("generate_to_checklist")
    private Integer generateToChecklist;

    /**
     * 状态：ENABLED / DISABLED
     */
    @TableField("status")
    private String status;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;
}