package com.mobe.finance.dto.request;

import lombok.Data;

@Data
public class FinanceRecordPageRequest {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    /**
     * INCOME / EXPENSE
     */
    private String type;

    /**
     * 分类精确筛选
     */
    private String category;

    /**
     * 模糊搜索：
     * remark / category / type / amount字符串 / 日期字符串
     */
    private String keyword;

    /**
     * NONE / QUICK / RANGE / MONTH
     */
    private String dateMode;

    /**
     * TODAY / THIS_WEEK / THIS_MONTH / LAST_30_DAYS
     */
    private String quickDate;

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    private String startDate;

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    private String endDate;

    /**
     * yyyy-MM
     */
    private String month;
}