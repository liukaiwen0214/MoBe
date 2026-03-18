package com.mobe.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserOwnedEntity extends BaseEntity {

    @TableField("user_id")
    private String userId;
}