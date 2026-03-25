package com.mobe.task.controller;

import com.mobe.common.result.ApiResponse;
import com.mobe.task.dto.request.TaskCreateRequest;
import com.mobe.task.dto.response.TaskSimpleResponse;
import com.mobe.task.service.TaskService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Resource
    private TaskService taskService;

    /**
     * 查询任务列表（简单）
     */
    @GetMapping("/simple-list")
    public ApiResponse<List<TaskSimpleResponse>> listSimpleTasks(HttpServletRequest httpServletRequest) {
        log.info("收到查询任务简单列表请求");
        List<TaskSimpleResponse> response = taskService.listSimpleTasks(httpServletRequest);
        return ApiResponse.success("查询成功", response);
    }

    /**
     * 新增任务
     */
    @PostMapping("/create")
    public ApiResponse<Boolean> createTask(@RequestBody TaskCreateRequest request,
                                           HttpServletRequest httpServletRequest) {
        log.info("收到新增任务请求");
        taskService.createTask(request, httpServletRequest);
        return ApiResponse.success("新增成功", true);
    }
}