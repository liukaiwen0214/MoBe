package com.mobe.checklist.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 清单执行项响应
 */
@Data
public class ChecklistExecutionResponse {

    /**
     * 执行ID
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
     * 所属习惯ID
     */
    private String habitId;

    /**
     * 所属习惯名称
     */
    private String habitName;

    /**
     * 清单定义ID
     */
    private String checklistId;

    /**
     * 标题
     */
    private String title;

    /**
     * 说明
     */
    private String description;

    /**
     * 状态：PENDING / DONE / SKIPPED / MISSED
     */
    private String status;

    /**
     * 执行日期
     */
    private LocalDate executeDate;

    /**
     * 执行时间
     */
    private LocalTime executeTime;

    /**
     * 备注
     */
    private String note;

    /**
     * 排序
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