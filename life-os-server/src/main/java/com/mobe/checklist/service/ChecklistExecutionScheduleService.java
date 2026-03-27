package com.mobe.checklist.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mobe.checklist.entity.ChecklistEntity;
import com.mobe.checklist.entity.ChecklistExecutionEntity;
import com.mobe.checklist.mapper.ChecklistExecutionMapper;
import com.mobe.checklist.mapper.ChecklistMapper;
import com.mobe.habit.entity.HabitEntity;
import com.mobe.habit.mapper.HabitMapper;
import com.mobe.task.entity.TaskEntity;
import com.mobe.task.mapper.TaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChecklistExecutionScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ChecklistExecutionScheduleService.class);

    private final HabitMapper habitMapper;
    private final ChecklistMapper checklistMapper;
    private final ChecklistExecutionMapper checklistExecutionMapper;
    private final TaskMapper taskMapper;

    public ChecklistExecutionScheduleService(HabitMapper habitMapper,
                                            ChecklistMapper checklistMapper,
                                            ChecklistExecutionMapper checklistExecutionMapper,
                                            TaskMapper taskMapper) {
        this.habitMapper = habitMapper;
        this.checklistMapper = checklistMapper;
        this.checklistExecutionMapper = checklistExecutionMapper;
        this.taskMapper = taskMapper;
    }

    /**
     * 定时任务1：自动生成当天清单
     */
    @Transactional(rollbackFor = Exception.class)
    public void generateTodayChecklistExecutions() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<HabitEntity> habitList = habitMapper.selectList(
                new LambdaQueryWrapper<HabitEntity>()
                        .eq(HabitEntity::getIsDeleted, 0)
                        .eq(HabitEntity::getStatus, "ENABLED")
                        .eq(HabitEntity::getGenerateToChecklist, 1)
                        .orderByAsc(HabitEntity::getSort)
                        .orderByDesc(HabitEntity::getCreatedAt));

        if (habitList == null || habitList.isEmpty()) {
            log.info("generateTodayChecklistExecutions: no enabled habits need generate, date={}", today);
            return;
        }

        Set<String> taskIds = habitList.stream()
                .map(HabitEntity::getTaskId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, TaskEntity> taskMap = taskIds.isEmpty()
                ? Collections.emptyMap()
                : taskMapper.selectList(
                                new LambdaQueryWrapper<TaskEntity>()
                                        .in(TaskEntity::getId, taskIds)
                                        .eq(TaskEntity::getIsDeleted, 0))
                        .stream()
                        .collect(Collectors.toMap(TaskEntity::getId, item -> item));

        int generatedCount = 0;
        int skippedCount = 0;

        for (HabitEntity habit : habitList) {
            try {
                if (!shouldGenerateForDate(habit, today)) {
                    skippedCount++;
                    continue;
                }

                Long existedCount = checklistExecutionMapper.selectCount(
                        new LambdaQueryWrapper<ChecklistExecutionEntity>()
                                .eq(ChecklistExecutionEntity::getUserId, habit.getUserId())
                                .eq(ChecklistExecutionEntity::getHabitId, habit.getId())
                                .eq(ChecklistExecutionEntity::getExecuteDate, today)
                                .eq(ChecklistExecutionEntity::getIsDeleted, 0));

                if (existedCount != null && existedCount > 0) {
                    skippedCount++;
                    continue;
                }

                String checklistId = getOrCreateChecklistId(habit, taskMap.get(habit.getTaskId()), now);

                ChecklistExecutionEntity execution = new ChecklistExecutionEntity();
                execution.setChecklistId(checklistId);
                execution.setTaskId(habit.getTaskId());
                execution.setHabitId(habit.getId());
                execution.setExecuteDate(today);
                execution.setExecuteTime(habit.getReminderTime());
                execution.setStatus("PENDING");
                execution.setNote(null);
                execution.setSort(habit.getSort() == null ? 0 : habit.getSort());
                execution.setUserId(habit.getUserId());
                execution.setIsDeleted(0);
                execution.setCreatedAt(now);
                execution.setUpdatedAt(now);

                checklistExecutionMapper.insert(execution);
                generatedCount++;
            } catch (Exception e) {
                log.error("generateTodayChecklistExecutions error, habitId={}, userId={}",
                        habit.getId(), habit.getUserId(), e);
            }
        }

        log.info("generateTodayChecklistExecutions finished, date={}, generatedCount={}, skippedCount={}",
                today, generatedCount, skippedCount);
    }

    /**
     * 定时任务2：自动处理过期数据
     * 规则：execute_date < today 且 status = PENDING -> MISSED
     */
    @Transactional(rollbackFor = Exception.class)
    public void expirePendingChecklistExecutions() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        int updated = checklistExecutionMapper.update(
                null,
                new LambdaUpdateWrapper<ChecklistExecutionEntity>()
                        .eq(ChecklistExecutionEntity::getIsDeleted, 0)
                        .eq(ChecklistExecutionEntity::getStatus, "PENDING")
                        .lt(ChecklistExecutionEntity::getExecuteDate, today)
                        // .le(ChecklistExecutionEntity::getExecuteDate, today)
                        .set(ChecklistExecutionEntity::getStatus, "MISSED")
                        .set(ChecklistExecutionEntity::getUpdatedAt, now)
        );

        log.info("expirePendingChecklistExecutions finished, today={}, updated={}", today, updated);
    }

    private String getOrCreateChecklistId(HabitEntity habit, TaskEntity task, LocalDateTime now) {
        if (StringUtils.hasText(habit.getChecklistId())) {
            ChecklistEntity existedChecklist = checklistMapper.selectOne(
                    new LambdaQueryWrapper<ChecklistEntity>()
                            .eq(ChecklistEntity::getId, habit.getChecklistId())
                            .eq(ChecklistEntity::getUserId, habit.getUserId())
                            .eq(ChecklistEntity::getIsDeleted, 0)
                            .last("LIMIT 1"));

            if (existedChecklist != null) {
                return existedChecklist.getId();
            }
        }

        ChecklistEntity checklistByHabit = checklistMapper.selectOne(
                new LambdaQueryWrapper<ChecklistEntity>()
                        .eq(ChecklistEntity::getHabitId, habit.getId())
                        .eq(ChecklistEntity::getUserId, habit.getUserId())
                        .eq(ChecklistEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (checklistByHabit != null) {
            habitMapper.update(
                    null,
                    new LambdaUpdateWrapper<HabitEntity>()
                            .eq(HabitEntity::getId, habit.getId())
                            .eq(HabitEntity::getUserId, habit.getUserId())
                            .eq(HabitEntity::getIsDeleted, 0)
                            .set(HabitEntity::getChecklistId, checklistByHabit.getId())
                            .set(HabitEntity::getUpdatedAt, now));
            return checklistByHabit.getId();
        }

        ChecklistEntity checklist = new ChecklistEntity();
        checklist.setTaskId(habit.getTaskId());
        checklist.setTaskName(task != null ? task.getName() : null);
        checklist.setHabitId(habit.getId());
        checklist.setHabitName(habit.getName());
        checklist.setTitle(habit.getName());
        checklist.setDescription(habit.getDescription());
        checklist.setUserId(habit.getUserId());
        checklist.setIsDeleted(0);
        checklist.setCreatedAt(now);
        checklist.setUpdatedAt(now);

        checklistMapper.insert(checklist);

        habitMapper.update(
                null,
                new LambdaUpdateWrapper<HabitEntity>()
                        .eq(HabitEntity::getId, habit.getId())
                        .eq(HabitEntity::getUserId, habit.getUserId())
                        .eq(HabitEntity::getIsDeleted, 0)
                        .set(HabitEntity::getChecklistId, checklist.getId())
                        .set(HabitEntity::getUpdatedAt, now));

        return checklist.getId();
    }

    private boolean shouldGenerateForDate(HabitEntity habit, LocalDate date) {
        if (habit == null) {
            return false;
        }

        if (!"ENABLED".equals(habit.getStatus())) {
            return false;
        }

        if (habit.getGenerateToChecklist() == null || habit.getGenerateToChecklist() != 1) {
            return false;
        }

        if (habit.getStartDate() != null && habit.getStartDate().isAfter(date)) {
            return false;
        }

        String frequencyType = habit.getFrequencyType();
        String frequencyValue = habit.getFrequencyValue();

        if (!StringUtils.hasText(frequencyType)) {
            return false;
        }

        if ("DAILY".equals(frequencyType)) {
            return true;
        }

        if ("WEEKLY".equals(frequencyType)) {
            return matchWeekly(date, frequencyValue);
        }

        if ("MONTHLY".equals(frequencyType)) {
            return matchMonthly(date, frequencyValue);
        }

        return false;
    }

    private boolean matchWeekly(LocalDate date, String frequencyValue) {
        if (!StringUtils.hasText(frequencyValue)) {
            return false;
        }

        String normalized = frequencyValue.trim().toUpperCase(Locale.ROOT);

        if ("EVERYDAY".equals(normalized)) {
            return true;
        }

        Set<Integer> weekDays = parseWeeklyValues(normalized);
        if (weekDays.isEmpty()) {
            return false;
        }

        int current = date.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        return weekDays.contains(current);
    }

    private boolean matchMonthly(LocalDate date, String frequencyValue) {
        if (!StringUtils.hasText(frequencyValue)) {
            return false;
        }

        Set<Integer> monthDays = parseMonthlyValues(frequencyValue);
        if (monthDays.isEmpty()) {
            return false;
        }

        return monthDays.contains(date.getDayOfMonth());
    }

    private Set<Integer> parseWeeklyValues(String frequencyValue) {
        Set<Integer> result = new HashSet<>();

        String[] parts = frequencyValue.split("[,，\\s]+");
        for (String raw : parts) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }

            String item = raw.trim().toUpperCase(Locale.ROOT);

            if (item.matches("\\d+")) {
                int val = Integer.parseInt(item);
                if (val >= 1 && val <= 7) {
                    result.add(val);
                }
                continue;
            }

            switch (item) {
                case "MON":
                case "MONDAY":
                case "周一":
                case "星期一":
                    result.add(DayOfWeek.MONDAY.getValue());
                    break;
                case "TUE":
                case "TUESDAY":
                case "周二":
                case "星期二":
                    result.add(DayOfWeek.TUESDAY.getValue());
                    break;
                case "WED":
                case "WEDNESDAY":
                case "周三":
                case "星期三":
                    result.add(DayOfWeek.WEDNESDAY.getValue());
                    break;
                case "THU":
                case "THURSDAY":
                case "周四":
                case "星期四":
                    result.add(DayOfWeek.THURSDAY.getValue());
                    break;
                case "FRI":
                case "FRIDAY":
                case "周五":
                case "星期五":
                    result.add(DayOfWeek.FRIDAY.getValue());
                    break;
                case "SAT":
                case "SATURDAY":
                case "周六":
                case "星期六":
                    result.add(DayOfWeek.SATURDAY.getValue());
                    break;
                case "SUN":
                case "SUNDAY":
                case "周日":
                case "星期日":
                case "周天":
                case "星期天":
                    result.add(DayOfWeek.SUNDAY.getValue());
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    private Set<Integer> parseMonthlyValues(String frequencyValue) {
        Set<Integer> result = new HashSet<>();

        String[] parts = frequencyValue.split("[,，\\s]+");
        for (String raw : parts) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }

            String item = raw.trim();
            if (!item.matches("\\d+")) {
                continue;
            }

            int day = Integer.parseInt(item);
            if (day >= 1 && day <= 31) {
                result.add(day);
            }
        }

        return result;
    }
}