package com.mobe.habit.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 习惯完整新增请求
 */
@Data
public class HabitCreateDetailRequest {

    /**
     * 所属任务ID
     */
    private String taskId;

    /**
     * 关联清单ID
     */
    private String checklistId;

    /**
     * 习惯名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 图标
     */
    private String icon;

    /**
     * 颜色
     */
    private String color;

    /**
     * 频率类型：DAILY / WEEKLY / MONTHLY
     */
    private String frequencyType;

    /**
     * 频率文案
     */
    private String frequencyText;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 提醒时间
     */
    private LocalTime reminderTime;

    /**
     * 是否生成到清单
     */
    private Boolean generateToTodo;

    /**
     * 状态：ENABLED / DISABLED
     */
    private String status;

    /**
     * 排序
     */
    private Integer sort;
}