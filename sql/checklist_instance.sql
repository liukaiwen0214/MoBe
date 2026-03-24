-- =========================
-- 4. 清单展示实例表
-- =========================
DROP TABLE IF EXISTS checklist_instance;
CREATE TABLE checklist_instance (
    id                VARCHAR(64) PRIMARY KEY COMMENT '主键ID',
    user_id           VARCHAR(64) NOT NULL COMMENT '所属用户ID',
    source_type       VARCHAR(20) NOT NULL COMMENT '来源类型：CHECKLIST/HABIT',
    source_id         VARCHAR(64) NOT NULL COMMENT '来源主键ID，对应 checklist_item.id 或 habit_item.id',
    task_id           VARCHAR(64) DEFAULT NULL COMMENT '所属任务ID，可为空',
    task_name         VARCHAR(100) DEFAULT NULL COMMENT '所属任务名称，冗余提高查询效率',
    title             VARCHAR(200) NOT NULL COMMENT '展示标题',
    description       VARCHAR(500) DEFAULT NULL COMMENT '展示说明',
    priority          VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：HIGH/MEDIUM/LOW',
    frequency         VARCHAR(20) DEFAULT NULL COMMENT '展示频率：ONCE/DAILY/WEEKLY/MONTHLY',
    reminder_text     VARCHAR(100) DEFAULT NULL COMMENT '提醒文案',
    action_text       VARCHAR(100) DEFAULT NULL COMMENT '动作按钮文案',
    action_type       VARCHAR(50) DEFAULT NULL COMMENT '动作类型',
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/DONE',
    instance_date     DATE DEFAULT NULL COMMENT '实例日期，habit可按日/周/月展开时用',
    sort              INT NOT NULL DEFAULT 0 COMMENT '排序值',
    is_deleted        TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    created_at        DATETIME NOT NULL COMMENT '创建时间',
    updated_at        DATETIME NOT NULL COMMENT '更新时间',
    completed_at      DATETIME DEFAULT NULL COMMENT '完成时间'
) COMMENT='清单展示实例表';

CREATE INDEX idx_checklist_instance_user_id ON checklist_instance(user_id);
CREATE INDEX idx_checklist_instance_source ON checklist_instance(source_type, source_id);
CREATE INDEX idx_checklist_instance_task_id ON checklist_instance(task_id);
CREATE INDEX idx_checklist_instance_status ON checklist_instance(status);
CREATE INDEX idx_checklist_instance_instance_date ON checklist_instance(instance_date);
CREATE INDEX idx_checklist_instance_is_deleted ON checklist_instance(is_deleted);