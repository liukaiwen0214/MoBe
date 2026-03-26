package com.mobe.habit.dto.request;

import lombok.Data;

/**
 * 切换是否生成到清单请求
 */
@Data
public class HabitToggleGenerateRequest {

    /**
     * 习惯ID
     */
    private String id;

    /**
     * 是否生成到清单
     */
    private Boolean generateToTodo;
}