package com.mobe.habit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mobe.checklist.entity.ChecklistEntity;
import com.mobe.checklist.entity.ChecklistExecutionEntity;
import com.mobe.common.exception.BizException;
import com.mobe.habit.dto.request.HabitCreateDetailRequest;
import com.mobe.habit.dto.request.HabitCreateRequest;
import com.mobe.habit.dto.request.HabitListRequest;
import com.mobe.habit.dto.request.HabitPageRequest;
import com.mobe.habit.dto.request.HabitRecordPageRequest;
import com.mobe.habit.dto.request.HabitTimelinePageRequest;
import com.mobe.habit.dto.request.HabitToggleGenerateRequest;
import com.mobe.habit.dto.request.HabitUpdateRequest;
import com.mobe.habit.dto.response.HabitPageItemResponse;
import com.mobe.habit.dto.response.HabitRecordItemResponse;
import com.mobe.habit.dto.response.HabitSimpleResponse;
import com.mobe.habit.dto.response.HabitStatsResponse;
import com.mobe.habit.dto.response.HabitTimelineItemResponse;
import com.mobe.common.result.PageResult;
import com.mobe.finance.dto.response.PageResponse;
import com.mobe.checklist.mapper.ChecklistMapper;
import com.mobe.habit.mapper.HabitRecordMapper;
import com.mobe.habit.entity.HabitRecordEntity;
import com.mobe.habit.entity.HabitEntity;
import com.mobe.habit.mapper.HabitMapper;
import com.mobe.task.entity.TaskEntity;
import com.mobe.task.mapper.TaskMapper;
import com.mobe.user.dto.UserMeResponse;
import com.mobe.user.service.UserService;
import com.mobe.checklist.mapper.ChecklistExecutionMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 习惯服务类
 * <p>
 * 文件用途：实现习惯相关的业务逻辑，包括习惯的创建、查询、更新、删除、统计等功能
 * 所属模块：habit（习惯模块）
 * 核心职责：处理习惯相关的业务逻辑，提供习惯管理、记录查询、统计分析等功能
 * 与其他模块的关联：依赖于habitMapper、habitRecordMapper等数据访问层组件，使用userService进行用户认证，与checklist模块和task模块有业务关联
 * 在整体业务流程中的位置：位于服务层，是习惯相关业务逻辑的核心实现
 * 说明：使用 @Service 注解标记为服务类
 */
@Slf4j
@Service
public class HabitService {

    /**
     * 习惯 Mapper
     */
    private final HabitMapper habitMapper;
    private final ChecklistMapper checklistMapper;
    private final HabitRecordMapper habitRecordMapper;
    private final ChecklistExecutionMapper checklistExecutionMapper;
    /**
     * 用户服务
     */
    private final UserService userService;

    /**
     * 任务 Mapper
     */
    private final TaskMapper taskMapper;

    /**
     * 构造方法
     */
    public HabitService(HabitMapper habitMapper, ChecklistMapper checklistMapper, HabitRecordMapper habitRecordMapper,
            ChecklistExecutionMapper checklistExecutionMapper,
            TaskMapper taskMapper, UserService userService) {
        this.habitMapper = habitMapper;
        this.checklistMapper = checklistMapper;
        this.habitRecordMapper = habitRecordMapper;
        this.userService = userService;
        this.taskMapper = taskMapper;
        this.checklistExecutionMapper = checklistExecutionMapper;
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
                        .last("LIMIT 1"));

        if (task == null) {
            throw new BizException("所属任务不存在");
        }

        HabitEntity existed = habitMapper.selectOne(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getUserId, currentUser.getId())
                        .eq(HabitEntity::getTaskId, taskId)
                        .eq(HabitEntity::getName, habitName)
                        .eq(HabitEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

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

    public PageResult<HabitPageItemResponse> pageHabits(HabitPageRequest request,
            HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BizException("当前登录用户不存在");
        }

        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();

        Page<HabitEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<HabitEntity> queryWrapper = new LambdaQueryWrapper<HabitEntity>()
                .eq(HabitEntity::getUserId, currentUser.getId())
                .eq(HabitEntity::getIsDeleted, 0)
                .orderByAsc(HabitEntity::getSort)
                .orderByDesc(HabitEntity::getCreatedAt);

        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(HabitEntity::getName, keyword)
                    .or()
                    .like(HabitEntity::getDescription, keyword));
        }

        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq(HabitEntity::getStatus, request.getStatus().trim());
        }

        if (StringUtils.hasText(request.getFrequencyType())) {
            queryWrapper.eq(HabitEntity::getFrequencyType, request.getFrequencyType().trim());
        }

        if (request.getGenerateToTodo() != null) {
            queryWrapper.eq(HabitEntity::getGenerateToChecklist, request.getGenerateToTodo() ? 1 : 0);
        }

        Page<HabitEntity> habitPage = habitMapper.selectPage(page, queryWrapper);
        List<HabitEntity> habitList = habitPage.getRecords();
        if (habitList == null || habitList.isEmpty()) {
            return PageResult.of(pageNum, pageSize, habitPage.getTotal(), Collections.emptyList());
        }
        List<String> habitIds = habitList.stream()
                .map(HabitEntity::getId)
                .filter(StringUtils::hasText)
                .toList();

        Map<String, HabitStatsResponse> habitStatsMap = buildHabitStatsMap(habitIds, currentUser.getId());
        Set<String> taskIds = habitList.stream()
                .map(HabitEntity::getTaskId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Set<String> checklistIds = habitList.stream()
                .map(HabitEntity::getChecklistId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, TaskEntity> taskMap = taskIds.isEmpty()
                ? Collections.emptyMap()
                : taskMapper.selectList(new LambdaQueryWrapper<TaskEntity>()
                        .in(TaskEntity::getId, taskIds)
                        .eq(TaskEntity::getIsDeleted, 0))
                        .stream()
                        .collect(Collectors.toMap(TaskEntity::getId, item -> item));

        Map<String, ChecklistEntity> checklistMap = checklistIds.isEmpty()
                ? Collections.emptyMap()
                : checklistMapper.selectList(new LambdaQueryWrapper<ChecklistEntity>()
                        .in(ChecklistEntity::getId, checklistIds)
                        .eq(ChecklistEntity::getIsDeleted, 0))
                        .stream()
                        .collect(Collectors.toMap(ChecklistEntity::getId, item -> item));

        List<HabitPageItemResponse> records = habitList.stream().map(habit -> {
            HabitPageItemResponse item = new HabitPageItemResponse();
            item.setId(habit.getId());
            item.setTaskId(habit.getTaskId());
            item.setChecklistId(habit.getChecklistId());
            item.setName(habit.getName());
            item.setDescription(habit.getDescription());
            item.setIcon(habit.getIcon());
            item.setColor(habit.getColor());
            item.setFrequencyType(habit.getFrequencyType());
            item.setFrequencyText(habit.getFrequencyValue());
            item.setStartDate(habit.getStartDate());
            item.setReminderTime(habit.getReminderTime());
            item.setGenerateToTodo(habit.getGenerateToChecklist() != null && habit.getGenerateToChecklist() == 1);
            item.setStatus(habit.getStatus());
            item.setSort(habit.getSort());
            item.setCreatedAt(habit.getCreatedAt());
            item.setUpdatedAt(habit.getUpdatedAt());

            TaskEntity task = taskMap.get(habit.getTaskId());
            if (task != null) {
                item.setTaskName(task.getName());
            }

            ChecklistEntity checklist = checklistMap.get(habit.getChecklistId());
            if (checklist != null) {
                item.setChecklistTitle(checklist.getTitle());
            }

            HabitStatsResponse stats = habitStatsMap.get(habit.getId());

            item.setTotalCheckInCount(stats != null ? stats.getTotalCheckInCount() : 0);
            item.setStreakCount(stats != null ? stats.getStreakCount() : 0);
            item.setLongestStreakCount(stats != null ? stats.getLongestStreakCount() : 0);
            item.setLastCheckInAt(stats != null ? stats.getLastCheckInAt() : null);

            return item;
        }).toList();

        return PageResult.of(pageNum, pageSize, habitPage.getTotal(), records);
    }

    public String createHabitDetail(HabitCreateDetailRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BizException("当前登录用户不存在");
        }

        if (!StringUtils.hasText(request.getName())) {
            throw new BizException("习惯名称不能为空");
        }

        if (!StringUtils.hasText(request.getFrequencyType())) {
            throw new BizException("频率类型不能为空");
        }

        if (!"DAILY".equals(request.getFrequencyType())
                && !"WEEKLY".equals(request.getFrequencyType())
                && !"MONTHLY".equals(request.getFrequencyType())) {
            throw new BizException("频率类型不合法");
        }

        if (StringUtils.hasText(request.getStatus())
                && !"ENABLED".equals(request.getStatus())
                && !"DISABLED".equals(request.getStatus())) {
            throw new BizException("状态不合法");
        }

        if (StringUtils.hasText(request.getTaskId())) {
            TaskEntity task = taskMapper.selectOne(
                    new LambdaQueryWrapper<TaskEntity>()
                            .eq(TaskEntity::getId, request.getTaskId())
                            .eq(TaskEntity::getUserId, currentUser.getId())
                            .eq(TaskEntity::getIsDeleted, 0)
                            .last("LIMIT 1"));
            if (task == null) {
                throw new BizException("所属任务不存在");
            }
        }

        if (StringUtils.hasText(request.getChecklistId())) {
            ChecklistEntity checklist = checklistMapper.selectOne(
                    new LambdaQueryWrapper<ChecklistEntity>()
                            .eq(ChecklistEntity::getId, request.getChecklistId())
                            .eq(ChecklistEntity::getUserId, currentUser.getId())
                            .eq(ChecklistEntity::getIsDeleted, 0)
                            .last("LIMIT 1"));
            if (checklist == null) {
                throw new BizException("关联清单不存在");
            }
        }

        HabitEntity habit = new HabitEntity();
        habit.setUserId(currentUser.getId());
        habit.setTaskId(StringUtils.hasText(request.getTaskId()) ? request.getTaskId().trim() : null);
        habit.setChecklistId(StringUtils.hasText(request.getChecklistId()) ? request.getChecklistId().trim() : null);
        habit.setName(request.getName().trim());
        habit.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        habit.setIcon(StringUtils.hasText(request.getIcon()) ? request.getIcon().trim() : null);
        habit.setColor(StringUtils.hasText(request.getColor()) ? request.getColor().trim() : null);
        habit.setFrequencyType(request.getFrequencyType().trim());
        habit.setFrequencyValue(
                StringUtils.hasText(request.getFrequencyText()) ? request.getFrequencyText().trim() : null);
        habit.setStartDate(request.getStartDate());
        habit.setReminderTime(request.getReminderTime());
        habit.setGenerateToChecklist(Boolean.TRUE.equals(request.getGenerateToTodo()) ? 1 : 0);
        habit.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "ENABLED");
        habit.setSort(request.getSort() == null ? 0 : request.getSort());
        habit.setIsDeleted(0);
        habit.setCreatedAt(LocalDateTime.now());
        habit.setUpdatedAt(LocalDateTime.now());

        habitMapper.insert(habit);
        return "新增习惯成功";
    }

    public String updateHabit(HabitUpdateRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BizException("当前登录用户不存在");
        }

        if (!StringUtils.hasText(request.getId())) {
            throw new BizException("习惯ID不能为空");
        }

        if (!StringUtils.hasText(request.getName())) {
            throw new BizException("习惯名称不能为空");
        }

        if (!StringUtils.hasText(request.getFrequencyType())) {
            throw new BizException("频率类型不能为空");
        }

        if (!"DAILY".equals(request.getFrequencyType())
                && !"WEEKLY".equals(request.getFrequencyType())
                && !"MONTHLY".equals(request.getFrequencyType())) {
            throw new BizException("频率类型不合法");
        }

        if (StringUtils.hasText(request.getStatus())
                && !"ENABLED".equals(request.getStatus())
                && !"DISABLED".equals(request.getStatus())) {
            throw new BizException("状态不合法");
        }

        HabitEntity habit = habitMapper.selectOne(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getId, request.getId())
                        .eq(HabitEntity::getUserId, currentUser.getId())
                        .eq(HabitEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));
        if (habit == null) {
            throw new BizException("习惯不存在");
        }

        if (StringUtils.hasText(request.getTaskId())) {
            TaskEntity task = taskMapper.selectOne(
                    new LambdaQueryWrapper<TaskEntity>()
                            .eq(TaskEntity::getId, request.getTaskId())
                            .eq(TaskEntity::getUserId, currentUser.getId())
                            .eq(TaskEntity::getIsDeleted, 0)
                            .last("LIMIT 1"));
            if (task == null) {
                throw new BizException("所属任务不存在");
            }
        }

        if (StringUtils.hasText(request.getChecklistId())) {
            ChecklistEntity checklist = checklistMapper.selectOne(
                    new LambdaQueryWrapper<ChecklistEntity>()
                            .eq(ChecklistEntity::getId, request.getChecklistId())
                            .eq(ChecklistEntity::getUserId, currentUser.getId())
                            .eq(ChecklistEntity::getIsDeleted, 0)
                            .last("LIMIT 1"));
            if (checklist == null) {
                throw new BizException("关联清单不存在");
            }
        }

        habit.setTaskId(StringUtils.hasText(request.getTaskId()) ? request.getTaskId().trim() : null);
        habit.setChecklistId(StringUtils.hasText(request.getChecklistId()) ? request.getChecklistId().trim() : null);
        habit.setName(request.getName().trim());
        habit.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        habit.setIcon(StringUtils.hasText(request.getIcon()) ? request.getIcon().trim() : null);
        habit.setColor(StringUtils.hasText(request.getColor()) ? request.getColor().trim() : null);
        habit.setFrequencyType(request.getFrequencyType().trim());
        habit.setFrequencyValue(
                StringUtils.hasText(request.getFrequencyText()) ? request.getFrequencyText().trim() : null);
        habit.setStartDate(request.getStartDate());
        habit.setReminderTime(request.getReminderTime());
        habit.setGenerateToChecklist(Boolean.TRUE.equals(request.getGenerateToTodo()) ? 1 : 0);
        habit.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : habit.getStatus());
        habit.setSort(request.getSort() == null ? 0 : request.getSort());
        habit.setUpdatedAt(LocalDateTime.now());

        habitMapper.updateById(habit);
        return "更新习惯成功";
    }

    public String deleteHabit(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BizException("当前登录用户不存在");
        }

        if (!StringUtils.hasText(id)) {
            throw new BizException("习惯ID不能为空");
        }

        HabitEntity habit = habitMapper.selectOne(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getId, id)
                        .eq(HabitEntity::getUserId, currentUser.getId())
                        .eq(HabitEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));
        if (habit == null) {
            throw new BizException("习惯不存在");
        }

        habit.setIsDeleted(1);
        habit.setUpdatedAt(LocalDateTime.now());
        habitMapper.updateById(habit);

        return "删除习惯成功";
    }

    public String enableHabit(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BizException("当前登录用户不存在");
        }

        if (!StringUtils.hasText(id)) {
            throw new BizException("习惯ID不能为空");
        }

        HabitEntity habit = habitMapper.selectOne(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getId, id)
                        .eq(HabitEntity::getUserId, currentUser.getId())
                        .eq(HabitEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));
        if (habit == null) {
            throw new BizException("习惯不存在");
        }

        habit.setStatus("ENABLED");
        habit.setUpdatedAt(LocalDateTime.now());
        habitMapper.updateById(habit);

        return "启用习惯成功";
    }

    public String disableHabit(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BizException("当前登录用户不存在");
        }

        if (!StringUtils.hasText(id)) {
            throw new BizException("习惯ID不能为空");
        }

        HabitEntity habit = habitMapper.selectOne(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getId, id)
                        .eq(HabitEntity::getUserId, currentUser.getId())
                        .eq(HabitEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));
        if (habit == null) {
            throw new BizException("习惯不存在");
        }

        habit.setStatus("DISABLED");
        habit.setUpdatedAt(LocalDateTime.now());
        habitMapper.updateById(habit);

        return "停用习惯成功";
    }

    public String toggleHabitGenerate(HabitToggleGenerateRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BizException("当前登录用户不存在");
        }

        if (request == null || !StringUtils.hasText(request.getId())) {
            throw new BizException("习惯ID不能为空");
        }

        if (request.getGenerateToTodo() == null) {
            throw new BizException("生成到清单状态不能为空");
        }

        HabitEntity habit = habitMapper.selectOne(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getId, request.getId())
                        .eq(HabitEntity::getUserId, currentUser.getId())
                        .eq(HabitEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));
        if (habit == null) {
            throw new BizException("习惯不存在");
        }

        habit.setGenerateToChecklist(Boolean.TRUE.equals(request.getGenerateToTodo()) ? 1 : 0);
        habit.setUpdatedAt(LocalDateTime.now());
        habitMapper.updateById(habit);

        return "更新生成到清单状态成功";
    }

    public PageResult<HabitRecordItemResponse> pageHabitRecords(HabitRecordPageRequest request,
            HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BizException("当前登录用户不存在");
        }

        if (request == null || !StringUtils.hasText(request.getHabitId())) {
            throw new BizException("习惯ID不能为空");
        }

        HabitEntity habit = habitMapper.selectOne(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getId, request.getHabitId())
                        .eq(HabitEntity::getUserId, currentUser.getId())
                        .eq(HabitEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));
        if (habit == null) {
            throw new BizException("习惯不存在");
        }

        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();

        Page<HabitRecordEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<HabitRecordEntity> queryWrapper = new LambdaQueryWrapper<HabitRecordEntity>()
                .eq(HabitRecordEntity::getHabitId, request.getHabitId())
                .eq(HabitRecordEntity::getUserId, currentUser.getId())
                .eq(HabitRecordEntity::getIsDeleted, 0)
                .orderByDesc(HabitRecordEntity::getRecordDate)
                .orderByDesc(HabitRecordEntity::getRecordTime)
                .orderByDesc(HabitRecordEntity::getCreatedAt);

        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq(HabitRecordEntity::getStatus, request.getStatus().trim());
        }

        if (request.getStartDate() != null) {
            queryWrapper.ge(HabitRecordEntity::getRecordDate, request.getStartDate());
        }

        if (request.getEndDate() != null) {
            queryWrapper.le(HabitRecordEntity::getRecordDate, request.getEndDate());
        }

        Page<HabitRecordEntity> recordPage = habitRecordMapper.selectPage(page, queryWrapper);

        List<HabitRecordItemResponse> records = recordPage.getRecords().stream().map(record -> {
            HabitRecordItemResponse item = new HabitRecordItemResponse();
            item.setId(record.getId());
            item.setHabitId(record.getHabitId());
            item.setTaskId(record.getTaskId());
            item.setChecklistExecutionId(record.getChecklistExecutionId());
            item.setRecordDate(record.getRecordDate());
            item.setRecordTime(record.getRecordTime());
            item.setStatus(record.getStatus());
            item.setSource(record.getSource());
            item.setNote(record.getNote());
            item.setCreatedAt(record.getCreatedAt());
            return item;
        }).toList();

        return PageResult.of(pageNum, pageSize, recordPage.getTotal(), records);
    }

    /**
     * 查询习惯统计
     */
    public HabitStatsResponse getHabitStats(String habitId, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        if (!StringUtils.hasText(habitId)) {
            throw new BizException("习惯ID不能为空");
        }

        HabitEntity habit = habitMapper.selectOne(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getId, habitId)
                        .eq(HabitEntity::getUserId, currentUser.getId())
                        .eq(HabitEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (habit == null) {
            throw new BizException("习惯不存在");
        }

        List<ChecklistExecutionEntity> executionList = checklistExecutionMapper.selectList(
                new LambdaQueryWrapper<ChecklistExecutionEntity>()
                        .eq(ChecklistExecutionEntity::getUserId, currentUser.getId())
                        .eq(ChecklistExecutionEntity::getHabitId, habitId)
                        .eq(ChecklistExecutionEntity::getIsDeleted, 0));

        HabitStatsResponse response = new HabitStatsResponse();
        response.setHabitId(habitId);

        if (executionList == null || executionList.isEmpty()) {
            response.setTotalCheckInCount(0);
            response.setStreakCount(0);
            response.setLongestStreakCount(0);
            response.setLastCheckInAt(null);
            return response;
        }

        List<ChecklistExecutionEntity> doneList = executionList.stream()
                .filter(item -> "DONE".equals(item.getStatus()))
                .toList();

        response.setTotalCheckInCount(doneList.size());
        response.setLastCheckInAt(calculateLastCheckInAt(doneList));
        response.setStreakCount(calculateCurrentStreak(executionList));
        response.setLongestStreakCount(calculateLongestStreak(executionList));

        return response;
    }

    private LocalDateTime calculateLastCheckInAt(List<ChecklistExecutionEntity> doneList) {
        if (doneList == null || doneList.isEmpty()) {
            return null;
        }

        return doneList.stream()
                .map(this::buildExecutionDateTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private Integer calculateCurrentStreak(List<ChecklistExecutionEntity> executionList) {
        if (executionList == null || executionList.isEmpty()) {
            return 0;
        }

        Map<LocalDate, String> dateStatusMap = executionList.stream()
                .filter(item -> item.getExecuteDate() != null)
                .collect(Collectors.toMap(
                        ChecklistExecutionEntity::getExecuteDate,
                        ChecklistExecutionEntity::getStatus,
                        this::pickHigherPriorityStatus));

        if (dateStatusMap.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = dateStatusMap.containsKey(today)
                ? today
                : dateStatusMap.keySet().stream().max(LocalDate::compareTo).orElse(today);

        int streak = 0;
        LocalDate cursor = startDate;

        while (true) {
            String status = dateStatusMap.get(cursor);

            if ("DONE".equals(status)) {
                streak++;
                cursor = cursor.minusDays(1);
                continue;
            }

            if (cursor.equals(today) && "PENDING".equals(status)) {
                cursor = cursor.minusDays(1);
                continue;
            }

            break;
        }

        return streak;
    }

    private Integer calculateLongestStreak(List<ChecklistExecutionEntity> executionList) {
        if (executionList == null || executionList.isEmpty()) {
            return 0;
        }

        Map<LocalDate, String> dateStatusMap = executionList.stream()
                .filter(item -> item.getExecuteDate() != null)
                .collect(Collectors.toMap(
                        ChecklistExecutionEntity::getExecuteDate,
                        ChecklistExecutionEntity::getStatus,
                        this::pickHigherPriorityStatus));

        if (dateStatusMap.isEmpty()) {
            return 0;
        }

        List<LocalDate> sortedDates = dateStatusMap.keySet().stream()
                .sorted()
                .toList();

        int longest = 0;
        int current = 0;
        LocalDate prevDate = null;

        for (LocalDate date : sortedDates) {
            String status = dateStatusMap.get(date);

            if (!"DONE".equals(status)) {
                current = 0;
                prevDate = date;
                continue;
            }

            if (prevDate == null) {
                current = 1;
            } else if (prevDate.plusDays(1).equals(date)) {
                current++;
            } else {
                current = 1;
            }

            longest = Math.max(longest, current);
            prevDate = date;
        }

        return longest;
    }

    private String pickHigherPriorityStatus(String status1, String status2) {
        return getStatusPriority(status1) <= getStatusPriority(status2) ? status1 : status2;
    }

    private int getStatusPriority(String status) {
        if ("DONE".equals(status)) {
            return 1;
        }
        if ("PENDING".equals(status)) {
            return 2;
        }
        if ("SKIPPED".equals(status)) {
            return 3;
        }
        if ("MISSED".equals(status)) {
            return 4;
        }
        return 99;
    }

    private LocalDateTime buildExecutionDateTime(ChecklistExecutionEntity entity) {
        if (entity == null) {
            return null;
        }

        if ("DONE".equals(entity.getStatus()) && entity.getCompletedAt() != null) {
            return entity.getCompletedAt();
        }

        if (entity.getExecuteDate() != null && entity.getExecuteTime() != null) {
            return LocalDateTime.of(entity.getExecuteDate(), entity.getExecuteTime());
        }

        if (entity.getExecuteDate() != null) {
            return LocalDateTime.of(entity.getExecuteDate(), LocalTime.of(23, 59, 59));
        }

        return entity.getCreatedAt();
    }

    /**
     * 分页查询习惯时间轴
     */
    public PageResponse<HabitTimelineItemResponse> pageHabitTimeline(HabitTimelinePageRequest request,
            HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        if (request == null || !StringUtils.hasText(request.getHabitId())) {
            throw new BizException("习惯ID不能为空");
        }

        HabitEntity habit = habitMapper.selectOne(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getId, request.getHabitId())
                        .eq(HabitEntity::getUserId, currentUser.getId())
                        .eq(HabitEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (habit == null) {
            throw new BizException("习惯不存在");
        }

        long pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        long pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();

        LambdaQueryWrapper<ChecklistExecutionEntity> wrapper = new LambdaQueryWrapper<ChecklistExecutionEntity>()
                .eq(ChecklistExecutionEntity::getUserId, currentUser.getId())
                .eq(ChecklistExecutionEntity::getHabitId, request.getHabitId())
                .eq(ChecklistExecutionEntity::getIsDeleted, 0);

        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(ChecklistExecutionEntity::getStatus, request.getStatus().trim());
        }

        if (request.getStartDate() != null) {
            wrapper.ge(ChecklistExecutionEntity::getExecuteDate, request.getStartDate());
        }

        if (request.getEndDate() != null) {
            wrapper.le(ChecklistExecutionEntity::getExecuteDate, request.getEndDate());
        }

        List<ChecklistExecutionEntity> executionList = checklistExecutionMapper.selectList(wrapper);

        if (executionList == null || executionList.isEmpty()) {
            PageResponse<HabitTimelineItemResponse> empty = new PageResponse<>();
            empty.setTotal(0L);
            empty.setPageNum(pageNum);
            empty.setPageSize(pageSize);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        List<HabitTimelineItemResponse> allRecords = executionList.stream()
                .map(entity -> {
                    HabitTimelineItemResponse item = new HabitTimelineItemResponse();
                    item.setId(entity.getId());
                    item.setHabitId(entity.getHabitId());
                    item.setTaskId(entity.getTaskId());
                    item.setChecklistExecutionId(entity.getId());
                    item.setStatus(entity.getStatus());
                    item.setSource("LIST");
                    item.setNote(entity.getNote());
                    item.setCreatedAt(entity.getCreatedAt());

                    if ("DONE".equals(entity.getStatus()) && entity.getCompletedAt() != null) {
                        item.setRecordDate(entity.getCompletedAt().toLocalDate());
                        item.setRecordTime(entity.getCompletedAt().toLocalTime().withNano(0));
                    } else {
                        item.setRecordDate(entity.getExecuteDate());
                        item.setRecordTime(entity.getExecuteTime());
                    }

                    return item;
                })
                .sorted((a, b) -> {
                    LocalDateTime timeA = buildTimelineDateTime(a.getRecordDate(), a.getRecordTime(), a.getCreatedAt());
                    LocalDateTime timeB = buildTimelineDateTime(b.getRecordDate(), b.getRecordTime(), b.getCreatedAt());
                    return timeB.compareTo(timeA);
                })
                .toList();

        int fromIndex = (int) ((pageNum - 1) * pageSize);
        if (fromIndex >= allRecords.size()) {
            PageResponse<HabitTimelineItemResponse> empty = new PageResponse<>();
            empty.setTotal((long) allRecords.size());
            empty.setPageNum(pageNum);
            empty.setPageSize(pageSize);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        int toIndex = Math.min(fromIndex + (int) pageSize, allRecords.size());
        List<HabitTimelineItemResponse> pageRecords = allRecords.subList(fromIndex, toIndex);

        PageResponse<HabitTimelineItemResponse> response = new PageResponse<>();
        response.setTotal((long) allRecords.size());
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setRecords(pageRecords);
        return response;
    }

    private LocalDateTime buildTimelineDateTime(LocalDate date, LocalTime time, LocalDateTime createdAt) {
        if (date != null && time != null) {
            return LocalDateTime.of(date, time);
        }
        if (date != null) {
            return LocalDateTime.of(date, LocalTime.of(23, 59, 59));
        }
        return createdAt == null ? LocalDateTime.MIN : createdAt;
    }

    /**
     * 批量计算习惯统计
     */
    private Map<String, HabitStatsResponse> buildHabitStatsMap(List<String> habitIds, String userId) {
        if (habitIds == null || habitIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ChecklistExecutionEntity> executionList = checklistExecutionMapper.selectList(
                new LambdaQueryWrapper<ChecklistExecutionEntity>()
                        .eq(ChecklistExecutionEntity::getUserId, userId)
                        .in(ChecklistExecutionEntity::getHabitId, habitIds)
                        .eq(ChecklistExecutionEntity::getIsDeleted, 0));

        if (executionList == null || executionList.isEmpty()) {
            return habitIds.stream().collect(Collectors.toMap(
                    id -> id,
                    id -> {
                        HabitStatsResponse stats = new HabitStatsResponse();
                        stats.setHabitId(id);
                        stats.setTotalCheckInCount(0);
                        stats.setStreakCount(0);
                        stats.setLongestStreakCount(0);
                        stats.setLastCheckInAt(null);
                        return stats;
                    }));
        }

        Map<String, List<ChecklistExecutionEntity>> groupedMap = executionList.stream()
                .filter(item -> StringUtils.hasText(item.getHabitId()))
                .collect(Collectors.groupingBy(ChecklistExecutionEntity::getHabitId));

        Map<String, HabitStatsResponse> result = new HashMap<>();

        for (String habitId : habitIds) {
            List<ChecklistExecutionEntity> oneHabitExecutions = groupedMap.getOrDefault(habitId,
                    Collections.emptyList());

            List<ChecklistExecutionEntity> doneList = oneHabitExecutions.stream()
                    .filter(item -> "DONE".equals(item.getStatus()))
                    .toList();

            HabitStatsResponse stats = new HabitStatsResponse();
            stats.setHabitId(habitId);
            stats.setTotalCheckInCount(doneList.size());
            stats.setLastCheckInAt(calculateLastCheckInAt(doneList));
            stats.setStreakCount(calculateCurrentStreak(oneHabitExecutions));
            stats.setLongestStreakCount(calculateLongestStreak(oneHabitExecutions));

            result.put(habitId, stats);
        }

        return result;
    }
}