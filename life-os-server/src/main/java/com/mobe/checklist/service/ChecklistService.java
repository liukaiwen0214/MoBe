package com.mobe.checklist.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mobe.checklist.dto.request.ChecklistCreateRequest;
import com.mobe.checklist.dto.request.ChecklistPageRequest;
import com.mobe.checklist.dto.request.ChecklistUpdateRequest;
import com.mobe.checklist.dto.response.ChecklistExecutionResponse;
import com.mobe.checklist.entity.ChecklistEntity;
import com.mobe.checklist.entity.ChecklistExecutionEntity;
import com.mobe.checklist.mapper.ChecklistExecutionMapper;
import com.mobe.checklist.mapper.ChecklistMapper;
import com.mobe.habit.entity.HabitEntity;
import com.mobe.habit.mapper.HabitMapper;
import com.mobe.task.entity.TaskEntity;
import com.mobe.task.mapper.TaskMapper;
import com.mobe.common.exception.BizException;
import com.mobe.finance.dto.response.PageResponse;
import com.mobe.user.dto.UserMeResponse;
import com.mobe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 清单服务类
 */
@Service
public class ChecklistService {

    private final ChecklistExecutionMapper checklistExecutionMapper;
    private final UserService userService;
    private final TaskMapper taskMapper;
    private final HabitMapper habitMapper;
    private final ChecklistMapper checklistMapper;

    public ChecklistService(
            ChecklistExecutionMapper checklistExecutionMapper,
            UserService userService,
            TaskMapper taskMapper,
            HabitMapper habitMapper,
            ChecklistMapper checklistMapper) {
        this.checklistExecutionMapper = checklistExecutionMapper;
        this.userService = userService;
        this.taskMapper = taskMapper;
        this.habitMapper = habitMapper;
        this.checklistMapper = checklistMapper;
    }

    /**
     * 分页查询清单
     */
    public PageResponse<ChecklistExecutionResponse> pageChecklists(ChecklistPageRequest request,
            HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        long pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        long pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();

        Page<ChecklistExecutionEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<ChecklistExecutionEntity> wrapper = new LambdaQueryWrapper<ChecklistExecutionEntity>()
                .eq(ChecklistExecutionEntity::getUserId, currentUser.getId())
                .eq(ChecklistExecutionEntity::getIsDeleted, 0)
                .orderByAsc(ChecklistExecutionEntity::getStatus)
                .orderByAsc(ChecklistExecutionEntity::getSort)
                .orderByDesc(ChecklistExecutionEntity::getExecuteDate)
                .orderByDesc(ChecklistExecutionEntity::getCreatedAt);

        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(ChecklistExecutionEntity::getStatus, request.getStatus());
        }

        if (request.getExecuteDate() != null) {
            wrapper.eq(ChecklistExecutionEntity::getExecuteDate, request.getExecuteDate());
        }

        Page<ChecklistExecutionEntity> entityPage = checklistExecutionMapper.selectPage(page, wrapper);
        List<ChecklistExecutionEntity> executionList = entityPage.getRecords();

        if (executionList.isEmpty()) {
            PageResponse<ChecklistExecutionResponse> empty = new PageResponse<>();
            empty.setTotal(entityPage.getTotal());
            empty.setPageNum(entityPage.getCurrent());
            empty.setPageSize(entityPage.getSize());
            empty.setRecords(List.of());
            return empty;
        }

        Set<String> taskIds = executionList.stream()
                .map(ChecklistExecutionEntity::getTaskId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Set<String> habitIds = executionList.stream()
                .map(ChecklistExecutionEntity::getHabitId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Set<String> checklistIds = executionList.stream()
                .map(ChecklistExecutionEntity::getChecklistId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        final Map<String, TaskEntity> taskMap = taskIds.isEmpty()
                ? Collections.emptyMap()
                : taskMapper.selectList(new LambdaQueryWrapper<TaskEntity>()
                        .in(TaskEntity::getId, taskIds)
                        .eq(TaskEntity::getIsDeleted, 0))
                        .stream()
                        .collect(Collectors.toMap(TaskEntity::getId, item -> item));

        final Map<String, HabitEntity> habitMap = habitIds.isEmpty()
                ? Collections.emptyMap()
                : habitMapper.selectList(new LambdaQueryWrapper<HabitEntity>()
                        .in(HabitEntity::getId, habitIds)
                        .eq(HabitEntity::getIsDeleted, 0))
                        .stream()
                        .collect(Collectors.toMap(HabitEntity::getId, item -> item));

        final Map<String, ChecklistEntity> checklistMap = checklistIds.isEmpty()
                ? Collections.emptyMap()
                : checklistMapper.selectList(new LambdaQueryWrapper<ChecklistEntity>()
                        .in(ChecklistEntity::getId, checklistIds)
                        .eq(ChecklistEntity::getIsDeleted, 0))
                        .stream()
                        .collect(Collectors.toMap(ChecklistEntity::getId, item -> item));

        String keyword = StringUtils.hasText(request.getKeyword()) ? request.getKeyword().trim().toLowerCase() : null;

        List<ChecklistExecutionResponse> records = executionList.stream()
                .map(entity -> {
                    ChecklistExecutionResponse response = new ChecklistExecutionResponse();
                    response.setId(entity.getId());
                    response.setTaskId(entity.getTaskId());
                    response.setHabitId(entity.getHabitId());
                    response.setChecklistId(entity.getChecklistId());
                    response.setStatus(entity.getStatus());
                    response.setExecuteDate(entity.getExecuteDate());
                    response.setExecuteTime(entity.getExecuteTime());
                    response.setNote(entity.getNote());
                    response.setSort(entity.getSort());
                    response.setCompletedAt(entity.getCompletedAt());
                    response.setCreatedAt(entity.getCreatedAt());

                    TaskEntity task = taskMap.get(entity.getTaskId());
                    if (task != null) {
                        response.setTaskName(task.getName());
                    }

                    HabitEntity habit = habitMap.get(entity.getHabitId());
                    if (habit != null) {
                        response.setHabitName(habit.getName());
                    }

                    ChecklistEntity checklist = checklistMap.get(entity.getChecklistId());
                    if (checklist != null) {
                        response.setTitle(checklist.getTitle());
                        response.setDescription(checklist.getDescription());
                    }

                    return response;
                })
                .filter(item -> {
                    if (!StringUtils.hasText(keyword)) {
                        return true;
                    }
                    String text = String.join(" ",
                            defaultString(item.getTaskName()),
                            defaultString(item.getHabitName()),
                            defaultString(item.getTitle()),
                            defaultString(item.getDescription()),
                            defaultString(item.getNote())).toLowerCase();
                    return text.contains(keyword);
                })
                .toList();

        PageResponse<ChecklistExecutionResponse> response = new PageResponse<>();
        response.setTotal(entityPage.getTotal());
        response.setPageNum(entityPage.getCurrent());
        response.setPageSize(entityPage.getSize());
        response.setRecords(records);
        return response;
    }

    /**
     * 新增清单执行项
     */
    public String createChecklist(ChecklistCreateRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        LocalDateTime now = LocalDateTime.now();

        // 1. 参数校验
        if (!StringUtils.hasText(request.getTaskId())) {
            throw new BizException("所属任务不能为空");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BizException("清单标题不能为空");
        }
        if (request.getExecuteDate() == null) {
            throw new BizException("执行日期不能为空");
        }

        // 2. 先插入 checklist
        ChecklistEntity checklist = new ChecklistEntity();
        checklist.setTaskId(request.getTaskId());
        checklist.setTaskName(request.getTaskName());
        checklist.setHabitId(request.getHabitId());
        checklist.setHabitName(request.getHabitName());
        checklist.setTitle(request.getTitle());
        checklist.setDescription(request.getDescription());
        checklist.setUserId(currentUser.getId());
        checklist.setIsDeleted(0);
        checklist.setCreatedAt(now);
        checklist.setUpdatedAt(now);

        checklistMapper.insert(checklist);

        // 3. 再插入 checklist_execution
        ChecklistExecutionEntity execution = new ChecklistExecutionEntity();
        execution.setChecklistId(checklist.getId());
        execution.setTaskId(request.getTaskId());
        execution.setHabitId(request.getHabitId());
        execution.setExecuteDate(request.getExecuteDate());

        if (request.getExecuteTime() != null) {
            execution.setExecuteTime(request.getExecuteTime());
        }

        execution.setStatus(request.getStatus());
        execution.setNote(request.getNote());
        execution.setSort(request.getSort() == null ? 0 : request.getSort());
        execution.setUserId(currentUser.getId());
        execution.setIsDeleted(0);
        execution.setCreatedAt(now);
        execution.setUpdatedAt(now);

        checklistExecutionMapper.insert(execution);

        return "新增成功";
    }

    /**
     * 完成清单执行项
     */
    public void completeChecklist(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        ChecklistExecutionEntity execution = checklistExecutionMapper.selectOne(
                new LambdaQueryWrapper<ChecklistExecutionEntity>()
                        .eq(ChecklistExecutionEntity::getId, id)
                        .eq(ChecklistExecutionEntity::getUserId, currentUser.getId())
                        .eq(ChecklistExecutionEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (execution == null) {
            throw new BizException("清单执行项不存在");
        }

        if (!"PENDING".equals(execution.getStatus())) {
            throw new BizException("当前状态不允许完成");
        }

        LocalDateTime now = LocalDateTime.now();
        execution.setStatus("DONE");
        execution.setCompletedAt(now);
        execution.setUpdatedAt(now);

        checklistExecutionMapper.updateById(execution);
    }

    /**
     * 恢复为未完成
     */
    public void restoreChecklist(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        ChecklistExecutionEntity execution = checklistExecutionMapper.selectOne(
                new LambdaQueryWrapper<ChecklistExecutionEntity>()
                        .eq(ChecklistExecutionEntity::getId, id)
                        .eq(ChecklistExecutionEntity::getUserId, currentUser.getId())
                        .eq(ChecklistExecutionEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (execution == null) {
            throw new BizException("清单执行项不存在");
        }

        if (!"DONE".equals(execution.getStatus())) {
            throw new BizException("当前状态不允许恢复");
        }

        LocalDateTime now = LocalDateTime.now();
        execution.setStatus("PENDING");
        execution.setCompletedAt(null);
        execution.setUpdatedAt(now);

        checklistExecutionMapper.updateById(execution);
    }

    /**
     * 跳过清单执行项
     */
    public void skipChecklist(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        ChecklistExecutionEntity execution = checklistExecutionMapper.selectOne(
                new LambdaQueryWrapper<ChecklistExecutionEntity>()
                        .eq(ChecklistExecutionEntity::getId, id)
                        .eq(ChecklistExecutionEntity::getUserId, currentUser.getId())
                        .eq(ChecklistExecutionEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (execution == null) {
            throw new BizException("清单执行项不存在");
        }

        if (!"PENDING".equals(execution.getStatus())) {
            throw new BizException("当前状态不允许跳过");
        }

        LocalDateTime now = LocalDateTime.now();
        execution.setStatus("SKIPPED");
        execution.setUpdatedAt(now);

        checklistExecutionMapper.updateById(execution);
    }

    /**
     * 删除清单执行项
     */
    public void deleteChecklist(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        ChecklistExecutionEntity execution = checklistExecutionMapper.selectOne(
                new LambdaQueryWrapper<ChecklistExecutionEntity>()
                        .eq(ChecklistExecutionEntity::getId, id)
                        .eq(ChecklistExecutionEntity::getUserId, currentUser.getId())
                        .eq(ChecklistExecutionEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (execution == null) {
            throw new BizException("清单执行项不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        execution.setIsDeleted(1);
        execution.setUpdatedAt(now);

        checklistExecutionMapper.updateById(execution);
    }

    public void updateChecklist(ChecklistUpdateRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        ChecklistExecutionEntity execution = checklistExecutionMapper.selectOne(
                new LambdaQueryWrapper<ChecklistExecutionEntity>()
                        .eq(ChecklistExecutionEntity::getId, request.getId())
                        .eq(ChecklistExecutionEntity::getUserId, currentUser.getId())
                        .eq(ChecklistExecutionEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (execution == null) {
            throw new BizException("清单执行项不存在");
        }

        execution.setTaskId(request.getTaskId());
        execution.setHabitId(request.getHabitId());
        execution.setExecuteDate(request.getExecuteDate());
        execution.setExecuteTime(request.getExecuteTime());
        execution.setNote(request.getNote());
        execution.setUpdatedAt(LocalDateTime.now());

        checklistExecutionMapper.updateById(execution);

        if (execution.getChecklistId() != null) {
            ChecklistEntity checklist = checklistMapper.selectOne(
                    new LambdaQueryWrapper<ChecklistEntity>()
                            .eq(ChecklistEntity::getId, execution.getChecklistId())
                            .eq(ChecklistEntity::getUserId, currentUser.getId())
                            .eq(ChecklistEntity::getIsDeleted, 0)
                            .last("LIMIT 1"));

            if (checklist != null) {
                checklist.setTaskId(request.getTaskId());
                checklist.setHabitId(request.getHabitId());
                checklist.setTaskName(request.getTaskName());
                checklist.setHabitName(request.getHabitName());
                checklist.setTitle(request.getTitle());
                checklist.setDescription(request.getDescription());
                checklist.setUpdatedAt(LocalDateTime.now());
                checklistMapper.updateById(checklist);
            }
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private UserMeResponse getLoginUserEntity(HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = userService.getCurrentUser(httpServletRequest);
        if (currentUser == null || !StringUtils.hasText(currentUser.getId())) {
            throw new BizException("用户未登录");
        }
        return currentUser;
    }
}