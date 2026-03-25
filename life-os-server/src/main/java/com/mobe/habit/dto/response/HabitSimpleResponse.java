package com.mobe.habit.dto.response;

import lombok.Data;

/**
 * 习惯简单响应
 */
@Data
public class HabitSimpleResponse {

    /**
     * 习惯ID
     */
    private String id;

    /**
     * 所属任务ID
     */
    private String taskId;

    /**
     * 习惯名称
     */
    private String name;
}