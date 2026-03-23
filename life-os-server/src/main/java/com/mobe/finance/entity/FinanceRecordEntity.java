package com.mobe.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务记录实体类
 * <p>
 * 功能：存储用户的财务记录信息，包括收入和支出
 * 说明：使用 @TableName 注解指定数据库表名
 */
@Data
@TableName("finance_records")
public class FinanceRecordEntity {

    /**
     * 流水ID
     * <p>
     * 说明：财务记录的唯一标识符
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     * <p>
     * 说明：关联到用户表的用户ID
     */
    private String userId;

    /**
     * 类型
     * <p>
     * 说明：财务记录的类型，INCOME 表示收入，EXPENSE 表示支出
     */
    private String type;

    /**
     * 分类名称
     * <p>
     * 说明：财务记录的分类，如餐饮、交通、工资等
     */
    private String category;

    /**
     * 金额
     * <p>
     * 说明：财务记录的金额，精确到两位小数
     */
    private BigDecimal amount;

    /**
     * 发生时间
     * <p>
     * 说明：财务记录的发生时间
     */
    private LocalDateTime recordDate;

    /**
     * 备注
     * <p>
     * 说明：财务记录的备注信息
     */
    private String remark;

    /**
     * 是否删除
     * <p>
     * 说明：0 表示未删除，1 表示已删除（软删除）
     */
    private Integer isDeleted;

    /**
     * 创建时间
     * <p>
     * 说明：记录的创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * <p>
     * 说明：记录的最后更新时间
     */
    private LocalDateTime updatedAt;
}