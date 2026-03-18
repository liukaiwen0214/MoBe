CREATE TABLE `user` (
                        `id` VARCHAR(32) NOT NULL COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                        `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
                        `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
                        `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
                        `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
                        `status` VARCHAR(20) NOT NULL COMMENT '状态',
                        `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_user_username` (`username`),
                        UNIQUE KEY `uk_user_email` (`email`),
                        KEY `idx_user_status` (`status`),
                        KEY `idx_user_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';