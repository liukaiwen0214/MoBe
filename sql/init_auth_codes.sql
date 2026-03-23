CREATE TABLE auth_codes (
                            id VARCHAR(32) NOT NULL COMMENT '验证码 ID',
                            target VARCHAR(100) NOT NULL COMMENT '目标，如邮箱、手机号',
                            code VARCHAR(20) NOT NULL COMMENT '验证码内容',
                            type VARCHAR(30) NOT NULL COMMENT '类型：REGISTER/PASSWORD_RESET/CAPTCHA',
                            expires_at DATETIME NOT NULL COMMENT '过期时间',
                            updated_at DATETIME NOT NULL COMMENT '更新时间',
                            created_at DATETIME NOT NULL COMMENT '创建时间',
                            is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 未删除，1 已删除',
                            status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/EXPIRED/USED',
                            used_at DATETIME DEFAULT NULL COMMENT '使用时间',
                            used TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已使用：0 否，1 是',
                            PRIMARY KEY (id),
                            KEY idx_auth_codes_target (target),
                            KEY idx_auth_codes_type (type),
                            KEY idx_auth_codes_expires_at (expires_at),
                            KEY idx_auth_codes_deleted (is_deleted)
) COMMENT='验证码表';