package com.mobe.habit.dto.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 习惯时间轴分页请求
 */
@Data
public class HabitTimelinePageRequest {

    /**
     * 页码
     */
    private Long pageNum;

    /**
     * 每页条数
     */
    private Long pageSize;

    /**
     * 习惯ID
     */
    private String habitId;

    /**
     * 状态：PENDING / DONE / SKIPPED / MISSED
     */
    private String status;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;
}