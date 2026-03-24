package com.mobe.finance.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {

    private Long total;
    private Long pageNum;
    private Long pageSize;
    private List<T> records;
}