package com.mobe.habit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mobe.checklist.entity.ChecklistEntity;
import com.mobe.common.exception.BizException;
import com.mobe.habit.dto.request.HabitCreateDetailRequest;
import com.mobe.habit.dto.request.HabitCreateRequest;
import com.mobe.habit.dto.request.HabitListRequest;
import com.mobe.habit.dto.request.HabitPageRequest;
import com.mobe.habit.dto.request.HabitRecordPageRequest;
import com.mobe.habit.dto.request.HabitToggleGenerateRequest;
import com.mobe.habit.dto.request.HabitUpdateRequest;
import com.mobe.habit.dto.response.HabitPageItemResponse;
import com.mobe.habit.dto.response.HabitRecordItemResponse;
import com.mobe.habit.dto.response.HabitSimpleResponse;
import com.mobe.common.result.PageResult;
import com.mobe.checklist.mapper.ChecklistMapper;
import com.mobe.habit.mapper.HabitRecordMapper;
import com.mobe.habit.entity.HabitRecordEntity;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final ChecklistMapper checklistMapper;
    private final HabitRecordMapper habitRecordMapper;
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
            TaskMapper taskMapper, UserService userService) {
        this.habitMapper = habitMapper;
        this.checklistMapper = checklistMapper;
        this.habitRecordMapper = habitRecordMapper;
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

        List<String> habitIdList = habitList.stream()
                .map(HabitEntity::getId)
                .filter(StringUtils::hasText)
                .toList();

        Map<String, Long> totalCheckInCountMap = new HashMap<>();
        Map<String, LocalDateTime> lastCheckInAtMap = new HashMap<>();

        if (!habitIdList.isEmpty()) {
            List<HabitRecordEntity> allRecordList = habitRecordMapper.selectList(
                    new LambdaQueryWrapper<HabitRecordEntity>()
                            .in(HabitRecordEntity::getHabitId, habitIdList)
                            .eq(HabitRecordEntity::getIsDeleted, 0)
                            .orderByDesc(HabitRecordEntity::getRecordDate)
                            .orderByDesc(HabitRecordEntity::getRecordTime)
                            .orderByDesc(HabitRecordEntity::getCreatedAt));

            Map<String, List<HabitRecordEntity>> recordGroupMap = allRecordList.stream()
                    .collect(Collectors.groupingBy(HabitRecordEntity::getHabitId));

            for (String habitId : habitIdList) {
                List<HabitRecordEntity> recordList = recordGroupMap.getOrDefault(habitId, Collections.emptyList());

                long totalCheckInCount = recordList.stream()
                        .filter(item -> "DONE".equals(item.getStatus()))
                        .count();
                totalCheckInCountMap.put(habitId, totalCheckInCount);

                HabitRecordEntity latestDoneRecord = recordList.stream()
                        .filter(item -> "DONE".equals(item.getStatus()))
                        .findFirst()
                        .orElse(null);

                if (latestDoneRecord != null && latestDoneRecord.getRecordDate() != null) {
                    LocalDateTime lastCheckInAt = latestDoneRecord.getRecordTime() != null
                            ? LocalDateTime.of(latestDoneRecord.getRecordDate(), latestDoneRecord.getRecordTime())
                            : latestDoneRecord.getRecordDate().atStartOfDay();
                    lastCheckInAtMap.put(habitId, lastCheckInAt);
                }
            }
        }

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

            item.setTotalCheckInCount(totalCheckInCountMap.getOrDefault(habit.getId(), 0L).intValue());
            item.setStreakCount(0);
            item.setLongestStreakCount(0);
            item.setLastCheckInAt(lastCheckInAtMap.get(habit.getId()));

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
}