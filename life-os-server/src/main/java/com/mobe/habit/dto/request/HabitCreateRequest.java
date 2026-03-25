package com.mobe.habit.dto.request;

import lombok.Data;

/**
 * 新增习惯请求
 */
@Data
public class HabitCreateRequest {

    /**
     * 所属任务ID
     */
    private String taskId;

    /**
     * 习惯名称
     */
    private String name;
}