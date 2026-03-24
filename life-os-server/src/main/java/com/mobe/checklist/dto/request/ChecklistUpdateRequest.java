package com.mobe.checklist.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新清单请求
 */
@Data
public class ChecklistUpdateRequest {

    /**
     * 清单ID
     */
    @NotBlank(message = "清单ID不能为空")
    private String id;

    /**
     * 所属任务ID
     */
    private String taskId;

    /**
     * 所属任务名称
     */
    @NotBlank(message = "所属任务名称不能为空")
    private String taskName;

    /**
     * 清单标题
     */
    @NotBlank(message = "清单标题不能为空")
    private String title;

    /**
     * 清单说明
     */
    private String description;

    /**
     * 优先级：HIGH/MEDIUM/LOW
     */
    private String priority;

    /**
     * 提醒文案
     */
    private String reminderText;

    /**
     * 动作文案
     */
    private String actionText;

    /**
     * 动作类型
     */
    private String actionType;

    /**
     * 排序值
     */
    private Integer sort;
}