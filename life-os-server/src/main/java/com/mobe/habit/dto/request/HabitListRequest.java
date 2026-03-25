package com.mobe.habit.dto.request;

import lombok.Data;

/**
 * 习惯简单查询请求
 */
@Data
public class HabitListRequest {

    /**
     * 所属任务ID
     */
    private String taskId;
}