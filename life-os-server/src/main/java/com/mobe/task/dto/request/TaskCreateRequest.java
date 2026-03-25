package com.mobe.task.dto.request;

import lombok.Data;

/**
 * 新增任务请求
 */
@Data
public class TaskCreateRequest {

    /**
     * 任务名称
     */
    private String name;
}