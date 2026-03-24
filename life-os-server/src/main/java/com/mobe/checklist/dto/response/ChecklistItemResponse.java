package com.mobe.checklist.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 清单项响应
 */
@Data
public class ChecklistItemResponse {

    /**
     * 清单ID
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
     * 清单标题
     */
    private String title;

    /**
     * 清单说明
     */
    private String description;

    /**
     * 优先级：HIGH/MEDIUM/LOW
     */
    private String priority;

    /**
     * 提醒文案
     */
    private String reminderText;

    /**
     * 动作文案
     */
    private String actionText;

    /**
     * 动作类型
     */
    private String actionType;

    /**
     * 状态：PENDING/DONE
     */
    private String status;

    /**
     * 来源类型：CHECKLIST/HABIT
     */
    private String sourceType;

    /**
     * 展示频率
     */
    private String frequency;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}