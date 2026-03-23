CREATE TABLE finance_records (
                                 id VARCHAR(32) NOT NULL COMMENT '流水 ID',
                                 user_id VARCHAR(32) NOT NULL COMMENT '用户 ID',
                                 type VARCHAR(20) NOT NULL COMMENT '类型：INCOME/EXPENSE',
                                 category VARCHAR(50) NOT NULL COMMENT '分类名称',
                                 amount DECIMAL(12,2) NOT NULL COMMENT '金额',
                                 record_date DATETIME NOT NULL COMMENT '发生时间',
                                 remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
                                 is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 未删除，1 已删除',
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (id),
                                 KEY idx_finance_records_user_id (user_id),
                                 KEY idx_finance_records_type (type),
                                 KEY idx_finance_records_category (category),
                                 KEY idx_finance_records_record_date (record_date),
                                 KEY idx_finance_records_deleted (is_deleted)
) COMMENT='账单流水表' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;