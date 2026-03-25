package com.mobe.habit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mobe.common.exception.BizException;
import com.mobe.habit.dto.request.HabitCreateRequest;
import com.mobe.habit.dto.request.HabitListRequest;
import com.mobe.habit.dto.response.HabitSimpleResponse;
import com.mobe.habit.entity.HabitEntity;
import com.mobe.habit.mapper.HabitMapper;
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
 * 习惯服务类
 */
@Slf4j
@Service
public class HabitService {

    /**
     * 习惯 Mapper
     */
    private final HabitMapper habitMapper;
    private final UserService userService;

    /**
     * 任务 Mapper
     */
    private final TaskMapper taskMapper;

    /**
     * 构造方法
     */
    public HabitService(HabitMapper habitMapper, TaskMapper taskMapper, UserService userService) {
        this.habitMapper = habitMapper;
        this.userService = userService;
        this.taskMapper = taskMapper;
    }

    /**
     * 查询习惯简单列表
     */
    public List<HabitSimpleResponse> listSimpleHabits(HabitListRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        LambdaQueryWrapper<HabitEntity> wrapper = new LambdaQueryWrapper<HabitEntity>()
                .eq(HabitEntity::getUserId, currentUser.getId())
                .eq(HabitEntity::getIsDeleted, 0)
                .orderByAsc(HabitEntity::getSort)
                .orderByDesc(HabitEntity::getCreatedAt);

        if (request != null && StringUtils.hasText(request.getTaskId())) {
            wrapper.eq(HabitEntity::getTaskId, request.getTaskId());
        }

        List<HabitEntity> habitList = habitMapper.selectList(wrapper);

        return habitList.stream().map(habit -> {
            HabitSimpleResponse response = new HabitSimpleResponse();
            response.setId(habit.getId());
            response.setTaskId(habit.getTaskId());
            response.setName(habit.getName());
            return response;
        }).toList();
    }

    /**
     * 新增习惯
     */
    public void createHabit(HabitCreateRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        if (request == null || !StringUtils.hasText(request.getTaskId())) {
            throw new BizException("所属任务不能为空");
        }

        if (!StringUtils.hasText(request.getName())) {
            throw new BizException("习惯名称不能为空");
        }

        String taskId = request.getTaskId();
        String habitName = request.getName().trim();

        TaskEntity task = taskMapper.selectOne(
                new LambdaQueryWrapper<TaskEntity>()
                        .eq(TaskEntity::getId, taskId)
                        .eq(TaskEntity::getUserId, currentUser.getId())
                        .eq(TaskEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BizException("所属任务不存在");
        }

        HabitEntity existed = habitMapper.selectOne(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getUserId, currentUser.getId())
                        .eq(HabitEntity::getTaskId, taskId)
                        .eq(HabitEntity::getName, habitName)
                        .eq(HabitEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (existed != null) {
            throw new BizException("习惯已存在");
        }

        HabitEntity entity = new HabitEntity();
        entity.setUserId(currentUser.getId());
        entity.setTaskId(taskId);
        entity.setChecklistId(null);
        entity.setName(habitName);
        entity.setDescription(null);
        entity.setIcon(null);
        entity.setColor(null);
        entity.setFrequencyType("DAILY");
        entity.setFrequencyValue("EVERYDAY");
        entity.setStartDate(null);
        entity.setReminderTime(null);
        entity.setGenerateToChecklist(1);
        entity.setStatus("ENABLED");
        entity.setSort(0);

        habitMapper.insert(entity);
    }
        private UserMeResponse getLoginUserEntity(HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = userService.getCurrentUser(httpServletRequest);
        if (currentUser == null || !StringUtils.hasText(currentUser.getId())) {
            throw new BizException("用户未登录");
        }
        return currentUser;
    }
}