package com.mobe.habit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 习惯执行记录实体
 * <p>
 * 对应表：habit_record
 */
@Data
@TableName("habit_record")
public class HabitRecordEntity {

    /**
     * 习惯记录ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

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
     * 是否删除：0未删除 1已删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}