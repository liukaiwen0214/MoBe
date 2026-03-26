package com.mobe.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页响应数据
 *
 * @param <T> 分页记录类型
 */
@Data
public class PageResult<T> {

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码
     */
    private int pageNum;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 快速创建分页响应对象
     */
    public static <T> PageResult<T> of(int pageNum, int pageSize, long total, List<T> records) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);
        pageResult.setTotal(total);
        pageResult.setRecords(records);
        return pageResult;
    }
}