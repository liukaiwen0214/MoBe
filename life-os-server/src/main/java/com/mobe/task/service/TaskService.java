package com.mobe.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mobe.common.exception.BizException;
import com.mobe.task.dto.request.TaskCreateRequest;
import com.mobe.task.dto.response.TaskSimpleResponse;
import com.mobe.task.entity.TaskEntity;
import com.mobe.task.mapper.TaskMapper;
import com.mobe.user.dto.UserMeResponse;
import com.mobe.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 任务服务类
 */
@Slf4j
@Service
public class TaskService {

    /**
     * 任务 Mapper
     */
    private final TaskMapper taskMapper;
    private final UserService userService;

    /**
     * 构造方法
     */
    public TaskService(TaskMapper taskMapper, UserService userService) {
        this.taskMapper = taskMapper;
        this.userService = userService;
    }

    /**
     * 查询任务简单列表
     */
    public List<TaskSimpleResponse> listSimpleTasks(HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        List<TaskEntity> taskList = taskMapper.selectList(
                new LambdaQueryWrapper<TaskEntity>()
                        .eq(TaskEntity::getUserId, currentUser.getId())
                        .eq(TaskEntity::getIsDeleted, 0)
                        .orderByAsc(TaskEntity::getSort)
                        .orderByDesc(TaskEntity::getCreatedAt)
        );

        return taskList.stream().map(task -> {
            TaskSimpleResponse response = new TaskSimpleResponse();
            response.setId(task.getId());
            response.setName(task.getName());
            return response;
        }).toList();
    }

    /**
     * 新增任务
     */
    public void createTask(TaskCreateRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new BizException("任务名称不能为空");
        }

        String taskName = request.getName().trim();

        TaskEntity existed = taskMapper.selectOne(
                new LambdaQueryWrapper<TaskEntity>()
                        .eq(TaskEntity::getUserId, currentUser.getId())
                        .eq(TaskEntity::getName, taskName)
                        .eq(TaskEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (existed != null) {
            throw new BizException("任务已存在");
        }

        TaskEntity entity = new TaskEntity();
        entity.setUserId(currentUser.getId());
        entity.setName(taskName);
        entity.setDescription(null);
        entity.setIcon(null);
        entity.setColor(null);
        entity.setStatus("ENABLED");
        entity.setSort(0);

        taskMapper.insert(entity);
    }
        private UserMeResponse getLoginUserEntity(HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = userService.getCurrentUser(httpServletRequest);
        if (currentUser == null || !StringUtils.hasText(currentUser.getId())) {
            throw new BizException("用户未登录");
        }
        return currentUser;
    }
}