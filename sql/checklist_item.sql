DROP TABLE IF EXISTS checklist_item;
CREATE TABLE checklist_item (
    id              VARCHAR(64) PRIMARY KEY COMMENT '主键ID',
    user_id         VARCHAR(64) NOT NULL COMMENT '所属用户ID',
    task_id         VARCHAR(64) DEFAULT NULL COMMENT '所属任务ID，可为空',
    task_name       VARCHAR(100) DEFAULT NULL COMMENT '所属任务名称，前期可冗余存储',
    title           VARCHAR(200) NOT NULL COMMENT '清单标题',
    description     VARCHAR(500) DEFAULT NULL COMMENT '清单说明',
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：HIGH/MEDIUM/LOW',
    reminder_text   VARCHAR(100) DEFAULT NULL COMMENT '提醒文案',
    action_text     VARCHAR(100) DEFAULT NULL COMMENT '动作按钮文案',
    action_type     VARCHAR(50) DEFAULT NULL COMMENT '动作类型，如 finance/note/task',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/DONE',
    sort            INT NOT NULL DEFAULT 0 COMMENT '排序值',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    created_at      DATETIME NOT NULL COMMENT '创建时间',
    updated_at      DATETIME NOT NULL COMMENT '更新时间',
    completed_at    DATETIME DEFAULT NULL COMMENT '完成时间'
) COMMENT='普通清单表';

CREATE INDEX idx_checklist_item_user_id ON checklist_item(user_id);
CREATE INDEX idx_checklist_item_task_id ON checklist_item(task_id);
CREATE INDEX idx_checklist_item_status ON checklist_item(status);
CREATE INDEX idx_checklist_item_is_deleted ON checklist_item(is_deleted);
