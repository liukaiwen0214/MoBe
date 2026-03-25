
-- =========================
-- habit_checkin_record
-- 习惯打卡记录表
-- =========================
DROP TABLE IF EXISTS `habit_checkin_record`;

CREATE TABLE `habit_checkin_record` (
  `id` VARCHAR(32) NOT NULL COMMENT '打卡记录ID',
  `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `habit_item_id` VARCHAR(32) NOT NULL COMMENT '习惯ID',
  `checklist_instance_id` VARCHAR(32) DEFAULT NULL COMMENT '关联清单实例ID，可空',

  `record_date` DATE NOT NULL COMMENT '记录日期',
  `record_time` TIME DEFAULT NULL COMMENT '记录时间，可空',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DONE' COMMENT '状态：DONE/MISSED/SKIPPED',
  `note` VARCHAR(500) DEFAULT NULL COMMENT '备注',

  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除 1已删除',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_habit_checkin_record_habit_date_deleted` (`habit_item_id`, `record_date`, `is_deleted`),
  KEY `idx_habit_checkin_record_user_id` (`user_id`),
  KEY `idx_habit_checkin_record_checklist_instance_id` (`checklist_instance_id`),
  KEY `idx_habit_checkin_record_status` (`status`),
  KEY `idx_habit_checkin_record_record_date` (`record_date`),
  KEY `idx_habit_checkin_record_deleted` (`is_deleted`)
) COMMENT='习惯打卡记录表' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;