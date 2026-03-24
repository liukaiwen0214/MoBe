DROP TABLE IF EXISTS habit_checkin_record;
CREATE TABLE habit_checkin_record (
    id                VARCHAR(64) PRIMARY KEY COMMENT '主键ID',
    user_id           VARCHAR(64) NOT NULL COMMENT '所属用户ID',
    habit_id          VARCHAR(64) NOT NULL COMMENT '习惯ID，对应 habit_item.id',
    checkin_date      DATE NOT NULL COMMENT '打卡日期',
    checkin_time      DATETIME NOT NULL COMMENT '实际打卡时间',
    period_type       VARCHAR(20) NOT NULL COMMENT '周期类型：DAILY/WEEKLY/MONTHLY',
    period_key        VARCHAR(32) NOT NULL COMMENT '周期标识，如 2026-03-24 / 2026-W13 / 2026-03',
    source            VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '来源：MANUAL/SYSTEM',
    remark            VARCHAR(255) DEFAULT NULL COMMENT '打卡备注',
    is_deleted        TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    created_at        DATETIME NOT NULL COMMENT '创建时间',
    updated_at        DATETIME NOT NULL COMMENT '更新时间'
) COMMENT='习惯打卡记录表';

CREATE INDEX idx_habit_checkin_record_user_id ON habit_checkin_record(user_id);
CREATE INDEX idx_habit_checkin_record_habit_id ON habit_checkin_record(habit_id);
CREATE INDEX idx_habit_checkin_record_checkin_date ON habit_checkin_record(checkin_date);
CREATE INDEX idx_habit_checkin_record_period_key ON habit_checkin_record(period_key);

-- 一个习惯在同一个周期内只能打一条有效记录
CREATE UNIQUE INDEX uk_habit_checkin_record_habit_period
ON habit_checkin_record(habit_id, period_key);