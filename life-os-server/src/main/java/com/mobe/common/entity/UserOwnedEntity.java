package com.mobe.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户拥有的实体类
 * <p>
 * 文件用途：作为所有属于特定用户的实体类的父类，提供用户关联字段
 * 所属模块：common（公共模块）
 * 核心职责：在基础实体的基础上，增加用户ID字段，用于标识实体的所属用户
 * 与其他模块的关联：被所有需要关联用户的业务模块的实体类继承，如习惯、财务、任务、清单等
 * 在整体业务流程中的位置：位于数据模型层，为需要用户关联的实体提供统一的用户标识结构
 * 说明：使用 @Data 注解自动生成 getter、setter、toString 等方法，使用 @EqualsAndHashCode 注解生成 equals 和 hashCode 方法
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserOwnedEntity extends BaseEntity {

    /**
     * 用户ID
     * <p>
     * 说明：关联到用户表的主键，标识实体的所属用户
     */
    @TableField("user_id")
    private String userId;
}