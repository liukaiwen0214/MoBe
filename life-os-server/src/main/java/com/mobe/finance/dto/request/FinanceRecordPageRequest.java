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

    private String category;

    private String keyword;

    /**
     * yyyy-MM-dd HH:mm:ss 也行，后面你自己再细化
     */
    private String startDate;

    private String endDate;

}