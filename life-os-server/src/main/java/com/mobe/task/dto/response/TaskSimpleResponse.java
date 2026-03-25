package com.mobe.task.dto.response;

import lombok.Data;

/**
 * 任务简单响应
 */
@Data
public class TaskSimpleResponse {

    /**
     * 任务ID
     */
    private String id;

    /**
     * 任务名称
     */
    private String name;
}