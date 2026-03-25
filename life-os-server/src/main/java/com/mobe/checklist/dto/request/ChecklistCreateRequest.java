package com.mobe.checklist.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 新增清单执行项请求
 */
@Data
public class ChecklistCreateRequest {

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
     * 标题
     */
    private String title;

    /**
     * 说明
     */
    private String description;

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
     * 状态：PENDING / DONE / SKIPPED / MISSED
     */
    private String status;

    /**
     * 排序
     */
    private Integer sort;
}