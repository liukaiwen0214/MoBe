package com.mobe.checklist.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 更新清单执行项请求
 */
@Data
public class ChecklistUpdateRequest {

    /**
     * 清单执行项ID
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
     * 清单标题
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
     * 排序
     */
    private Integer sort;
}