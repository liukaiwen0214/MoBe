package com.mobe.habit.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 习惯记录项响应
 */
@Data
public class HabitRecordItemResponse {

    /**
     * 记录ID
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
     * 状态：DONE / SKIPPED / MISSED
     */
    private String status;

    /**
     * 来源：MANUAL / SYSTEM / LIST
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