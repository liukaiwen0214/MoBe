package com.mobe.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页响应数据
 * <p>
 * 文件用途：封装分页查询的结果数据，提供统一的分页响应结构
 * 所属模块：common（公共模块）
 * 核心职责：封装分页相关的信息，包括总记录数、当前页码、每页大小和当前页数据
 * 与其他模块的关联：被所有需要分页查询的API使用，作为分页数据的标准返回格式
 * 在整体业务流程中的位置：位于响应处理层，为分页查询提供统一的响应结构
 * 说明：使用泛型 T 支持不同类型的分页数据，使用 @Data 注解自动生成 getter、setter、toString 等方法
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
     * <p>
     * 功能：根据分页参数和数据创建一个分页响应对象
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param total 总记录数
     * @param records 当前页数据列表
     * @param <T> 分页记录类型
     * @return PageResult<T> 分页响应对象
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