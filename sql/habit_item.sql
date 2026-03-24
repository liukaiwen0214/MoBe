DROP TABLE IF EXISTS habit_item;
CREATE TABLE habit_item (
    id                VARCHAR(64) PRIMARY KEY COMMENT '主键ID',
    user_id           VARCHAR(64) NOT NULL COMMENT '所属用户ID',
    task_id           VARCHAR(64) DEFAULT NULL COMMENT '所属任务ID，可为空',
    task_name         VARCHAR(100) DEFAULT NULL COMMENT '所属任务名称',
    title             VARCHAR(200) NOT NULL COMMENT '习惯标题',
    description       VARCHAR(500) DEFAULT NULL COMMENT '习惯说明',
    priority          VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：HIGH/MEDIUM/LOW',
    frequency_type    VARCHAR(20) NOT NULL COMMENT '频率类型：DAILY/WEEKLY/MONTHLY',
    frequency_value   VARCHAR(100) DEFAULT NULL COMMENT '频率配置，如周几/月几号，先字符串存',
    reminder_text     VARCHAR(100) DEFAULT NULL COMMENT '提醒文案',
    action_text       VARCHAR(100) DEFAULT NULL COMMENT '动作按钮文案',
    action_type       VARCHAR(50) DEFAULT NULL COMMENT '动作类型',
    is_enabled        TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0停用',
    is_deleted        TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    sort              INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at        DATETIME NOT NULL COMMENT '创建时间',
    updated_at        DATETIME NOT NULL COMMENT '更新时间'
) COMMENT='习惯定义表';

CREATE INDEX idx_habit_item_user_id ON habit_item(user_id);
CREATE INDEX idx_habit_item_task_id ON habit_item(task_id);
CREATE INDEX idx_habit_item_is_enabled ON habit_item(is_enabled);
CREATE INDEX idx_habit_item_is_deleted ON habit_item(is_deleted);
