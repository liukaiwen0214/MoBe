package com.mobe.checklist.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 清单定义实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checklist")
public class ChecklistEntity extends UserOwnedEntity {

    /**
     * 所属任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 对应习惯ID
     */
    @TableField("habit_id")
    private String habitId;

    /**
     * 清单标题
     */
    @TableField("title")
    private String title;

    /**
     * 清单描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态：ENABLED / DISABLED
     */
    @TableField("status")
    private String status;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 所属任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 对应习惯名称
     */
    @TableField("habit_name")
    private String habitName;


}