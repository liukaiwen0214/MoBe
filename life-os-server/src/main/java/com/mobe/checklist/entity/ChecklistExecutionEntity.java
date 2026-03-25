package com.mobe.checklist.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 清单执行实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checklist_execution")
public class ChecklistExecutionEntity extends UserOwnedEntity {

    /**
     * 所属任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 来源习惯ID
     */
    @TableField("habit_id")
    private String habitId;

    /**
     * 来源清单ID
     */
    @TableField("checklist_id")
    private String checklistId;

    /**
     * 执行日期
     */
    @TableField("execute_date")
    private LocalDate executeDate;

    /**
     * 执行时间
     */
    @TableField("execute_time")
    private LocalTime executeTime;

    /**
     * 状态：PENDING / DONE / SKIPPED / MISSED
     */
    @TableField("status")
    private String status;

    /**
     * 备注
     */
    @TableField("note")
    private String note;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

}