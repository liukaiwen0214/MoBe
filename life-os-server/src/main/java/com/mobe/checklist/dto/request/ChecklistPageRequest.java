package com.mobe.checklist.dto.request;

import lombok.Data;

/**
 * 清单分页查询请求
 */
@Data
public class ChecklistPageRequest {

    /**
     * 页码
     */
    private Long pageNum;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 状态：PENDING/DONE
     */
    private String status;

    /**
     * 优先级：HIGH/MEDIUM/LOW
     */
    private String priority;

    /**
     * 频率：ONCE/DAILY/WEEKLY/MONTHLY
     * 当前普通清单先固定 ONCE，字段先预留
     */
    private String frequency;

    /**
     * 是否仅看有提醒：1是 0否
     */
    private Integer reminderOnly;

    /**
     * 是否仅看有动作：1是 0否
     */
    private Integer actionOnly;
}