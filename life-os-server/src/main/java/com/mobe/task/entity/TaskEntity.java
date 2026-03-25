package com.mobe.task.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task")
public class TaskEntity extends UserOwnedEntity {

    /**
     * 任务名称
     */
    @TableField("name")
    private String name;

    /**
     * 任务描述
     */
    @TableField("description")
    private String description;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 颜色
     */
    @TableField("color")
    private String color;

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
}