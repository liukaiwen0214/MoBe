package com.mobe.habit.dto.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 习惯记录分页查询请求
 */
@Data
public class HabitRecordPageRequest {

    /**
     * 习惯ID
     */
    private String habitId;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 状态：DONE / SKIPPED / MISSED
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