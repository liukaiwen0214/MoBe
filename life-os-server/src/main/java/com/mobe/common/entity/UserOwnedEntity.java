package com.mobe.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户拥有的实体类
 * <p>
 * 功能：继承自 BaseEntity，用于表示属于特定用户的实体
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