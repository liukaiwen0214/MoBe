package com.mobe.habit.dto.request;

import lombok.Data;

/**
 * 习惯分页查询请求
 */
@Data
public class HabitPageRequest {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 状态：ENABLED / DISABLED
     */
    private String status;

    /**
     * 频率类型：DAILY / WEEKLY / MONTHLY
     */
    private String frequencyType;

    /**
     * 是否生成到清单
     */
    private Boolean generateToTodo;
}