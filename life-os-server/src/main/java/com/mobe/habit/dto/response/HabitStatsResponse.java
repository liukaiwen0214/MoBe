package com.mobe.habit.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 习惯统计响应
 */
@Data
public class HabitStatsResponse {

    /**
     * 习惯ID
     */
    private String habitId;

    /**
     * 累计打卡次数
     */
    private Integer totalCheckInCount;

    /**
     * 当前连续打卡次数
     */
    private Integer streakCount;

    /**
     * 最长连续打卡次数
     */
    private Integer longestStreakCount;

    /**
     * 最近一次打卡时间
     */
    private LocalDateTime lastCheckInAt;
}