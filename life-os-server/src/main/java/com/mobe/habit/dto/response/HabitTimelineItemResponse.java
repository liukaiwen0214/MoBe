package com.mobe.habit.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 习惯时间轴项响应
 */
@Data
public class HabitTimelineItemResponse {

    /**
     * 时间轴项ID
     */
    private String id;

    /**
     * 习惯ID
     */
    private String habitId;

    /**
     * 所属任务ID
     */
    private String taskId;

    /**
     * 关联清单执行ID
     */
    private String checklistExecutionId;

    /**
     * 记录日期
     */
    private LocalDate recordDate;

    /**
     * 记录时间
     */
    private LocalTime recordTime;

    /**
     * 状态：PENDING / DONE / SKIPPED / MISSED
     */
    private String status;

    /**
     * 来源：LIST
     */
    private String source;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}