package com.mobe.checklist.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 普通清单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checklist_item")
public class ChecklistItemEntity extends UserOwnedEntity {

    /**
     * 所属任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 所属任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 清单标题
     */
    @TableField("title")
    private String title;

    /**
     * 清单说明
     */
    @TableField("description")
    private String description;

    /**
     * 优先级：HIGH/MEDIUM/LOW
     */
    @TableField("priority")
    private String priority;

    /**
     * 提醒文案
     */
    @TableField("reminder_text")
    private String reminderText;

    /**
     * 动作文案
     */
    @TableField("action_text")
    private String actionText;

    /**
     * 动作类型
     */
    @TableField("action_type")
    private String actionType;

    /**
     * 状态：PENDING/DONE
     */
    @TableField("status")
    private String status;

    /**
     * 排序值
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;
}