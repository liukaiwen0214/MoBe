package com.mobe.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础实体类
 * <p>
 * 文件用途：作为所有实体类的父类，提供通用的基础字段
 * 所属模块：common（公共模块）
 * 核心职责：定义所有实体类共有的字段和属性
 * 与其他模块的关联：被所有业务模块的实体类继承，如用户、习惯、财务、任务、清单等
 * 在整体业务流程中的位置：位于数据模型层的最基础位置，为所有实体提供统一的基础结构
 * 说明：使用 @Data 注解自动生成 getter、setter、toString 等方法
 */
@Data
public class BaseEntity {

    /**
     * 主键ID
     * <p>
     * 说明：使用 UUID 作为主键，由 MyBatis Plus 自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 删除状态
     * <p>
     * 说明：0 表示未删除，1 表示已删除，由 MyBatis Plus 自动填充
     */
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Integer isDeleted;

    /**
     * 创建时间
     * <p>
     * 说明：记录实体创建的时间，由 MyBatis Plus 自动填充
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * <p>
     * 说明：记录实体最后更新的时间，由 MyBatis Plus 自动填充
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}