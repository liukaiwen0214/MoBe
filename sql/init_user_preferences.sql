CREATE TABLE user_preferences (
                                  id VARCHAR(32) NOT NULL COMMENT '偏好 ID',
                                  user_id VARCHAR(32) NOT NULL COMMENT '用户 ID',
                                  theme VARCHAR(20) DEFAULT 'light' COMMENT '主题：light/dark/system',
                                  language VARCHAR(20) DEFAULT 'zh-CN' COMMENT '语言',
                                  default_homepage VARCHAR(50) DEFAULT 'dashboard' COMMENT '默认首页',
                                  date_format VARCHAR(20) DEFAULT 'YYYY-MM-DD' COMMENT '日期格式',
                                  time_format VARCHAR(20) DEFAULT '24h' COMMENT '时间格式：12h/24h',
                                  week_start_day VARCHAR(10) DEFAULT 'MONDAY' COMMENT '每周起始日',
                                  notification_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否开启通知：1 开启，0 关闭',
                                  email_notification_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否开启邮件通知：1 开启，0 关闭',
                                  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 未删除，1 已删除',
                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (id),
                                  UNIQUE KEY uk_user_preferences_user_id (user_id),
                                  KEY idx_user_preferences_deleted (is_deleted)
) COMMENT='用户偏好配置表' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;