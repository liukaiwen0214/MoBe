-- =========================
-- habit_item
-- 习惯规则表
-- =========================
DROP TABLE IF EXISTS `habit_item`;

CREATE TABLE `habit_item` (
  `id` VARCHAR(32) NOT NULL COMMENT '习惯ID',
  `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `task_id` VARCHAR(32) DEFAULT NULL COMMENT '所属任务ID，可空',
  `checklist_item_id` VARCHAR(32) DEFAULT NULL COMMENT '对应清单定义ID，可空',

  `name` VARCHAR(100) NOT NULL COMMENT '习惯名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '习惯描述',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `color` VARCHAR(30) DEFAULT NULL COMMENT '颜色',

  `frequency_type` VARCHAR(20) NOT NULL COMMENT '频率类型：DAILY/WEEKLY/MONTHLY',
  `frequency_value` VARCHAR(100) DEFAULT NULL COMMENT '频率值，例如 EVERYDAY / 1,3,5 / 1,15,28',
  `start_date` DATE DEFAULT NULL COMMENT '开始日期',
  `reminder_time` TIME DEFAULT NULL COMMENT '提醒时间',

  `generate_to_checklist` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否生成到清单：1是 0否',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',

  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除 1已删除',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  KEY `idx_habit_item_user_id` (`user_id`),
  KEY `idx_habit_item_task_id` (`task_id`),
  KEY `idx_habit_item_checklist_item_id` (`checklist_item_id`),
  KEY `idx_habit_item_frequency_type` (`frequency_type`),
  KEY `idx_habit_item_status` (`status`),
  KEY `idx_habit_item_generate_to_checklist` (`generate_to_checklist`),
  KEY `idx_habit_item_start_date` (`start_date`),
  KEY `idx_habit_item_deleted` (`is_deleted`)
) COMMENT='习惯规则表' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;