package com.mobe.checklist.dto.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 清单执行项分页请求
 */
@Data
public class ChecklistPageRequest {

    /**
     * 页码
     */
    private Long pageNum;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 关键字：任务名 / 习惯名 / 标题 / 说明 / 备注
     */
    private String keyword;

    /**
     * 状态：PENDING / DONE / SKIPPED / MISSED
     */
    private String status;

    /**
     * 执行日期
     */
    private LocalDate executeDate;
}