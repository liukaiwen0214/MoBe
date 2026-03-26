package com.mobe.habit.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 习惯分页项响应
 */
@Data
public class HabitPageItemResponse {

    /**
     * 习惯ID
     */
    private String id;

    /**
     * 所属任务ID
     */
    private String taskId;

    /**
     * 所属任务名称
     */
    private String taskName;

    /**
     * 关联清单ID
     */
    private String checklistId;

    /**
     * 关联清单标题
     */
    private String checklistTitle;

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
     * 累计打卡次数
     */
    private Integer totalCheckInCount;

    /**
     * 当前连续打卡次数
     */
    private Integer streakCount;

    /**
     * 最长连续打卡次数
     */
    private Integer longestStreakCount;

    /**
     * 最近一次打卡时间
     */
    private LocalDateTime lastCheckInAt;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}