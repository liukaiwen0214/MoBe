package com.mobe.habit.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mobe.common.entity.UserOwnedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 习惯打卡记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("habit_checkin_record")
public class HabitCheckinRecordEntity extends UserOwnedEntity {

    /**
     * 习惯ID
     */
    @TableField("habit_id")
    private String habitId;

    /**
     * 打卡日期
     */
    @TableField("checkin_date")
    private LocalDate checkinDate;

    /**
     * 实际打卡时间
     */
    @TableField("checkin_time")
    private LocalDateTime checkinTime;

    /**
     * 周期类型：DAILY/WEEKLY/MONTHLY
     */
    @TableField("period_type")
    private String periodType;

    /**
     * 周期标识，如 2026-03-24 / 2026-W13 / 2026-03
     */
    @TableField("period_key")
    private String periodKey;

    /**
     * 来源：MANUAL/SYSTEM
     */
    @TableField("source")
    private String source;

    /**
     * 打卡备注
     */
    @TableField("remark")
    private String remark;
}