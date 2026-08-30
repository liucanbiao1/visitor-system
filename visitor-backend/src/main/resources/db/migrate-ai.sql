-- ===================================================
-- 增量迁移: AI 自动审批功能
-- 对已存在的 visitor_system 库执行: mysql -u root -p visitor_system < migrate-ai.sql
-- ===================================================

USE visitor_system;

-- 1. 系统配置表 (存 AI 审批开关)
CREATE TABLE IF NOT EXISTS sys_config (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '配置ID',
    config_key  VARCHAR(100)    NOT NULL                 COMMENT '配置键',
    config_value VARCHAR(255)   NOT NULL DEFAULT ''      COMMENT '配置值',
    description VARCHAR(255)    DEFAULT NULL             COMMENT '配置说明',
    updated_by  BIGINT          DEFAULT NULL             COMMENT '最后修改人ID',
    update_time DATETIME        DEFAULT NULL ON UPDATE NOW() COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key),
    CONSTRAINT fk_config_user FOREIGN KEY (updated_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

INSERT IGNORE INTO sys_config (config_key, config_value, description)
VALUES ('ai.review.enabled', 'false', 'AI自动审批开关 true/false');

-- 2. AI 审批日志表
CREATE TABLE IF NOT EXISTS ai_review_log (
    id             BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    appointment_id BIGINT        NOT NULL                 COMMENT '预约ID',
    visitor_name   VARCHAR(50)   DEFAULT NULL             COMMENT '访客姓名(冗余)',
    visitor_phone  VARCHAR(20)   DEFAULT NULL             COMMENT '访客手机号(冗余)',
    decision       TINYINT       DEFAULT NULL             COMMENT 'AI决定 1通过 2拒绝 NULL=调用失败',
    reason         VARCHAR(500)  DEFAULT NULL             COMMENT 'AI理由/拒绝原因',
    confidence     DECIMAL(5,2)  DEFAULT NULL             COMMENT '置信度 0-100',
    risk_level     VARCHAR(20)   DEFAULT NULL             COMMENT '风险等级 low/medium/high',
    latency_ms     INT           DEFAULT NULL             COMMENT 'AI调用耗时(毫秒)',
    model_name     VARCHAR(50)   DEFAULT NULL             COMMENT '模型名',
    success        TINYINT       NOT NULL DEFAULT 1       COMMENT '1调用成功 0调用失败',
    error_message  VARCHAR(500)  DEFAULT NULL             COMMENT '失败原因(超时/余额不足/解析失败等)',
    raw_response   TEXT          DEFAULT NULL             COMMENT '模型原始响应(截断至2000字符)',
    create_time    DATETIME      NOT NULL DEFAULT NOW()  COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_appointment_id (appointment_id),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_ai_log_appt FOREIGN KEY (appointment_id) REFERENCES appointment (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审批日志表';

-- 3. AI 审核人系统账号 (status=0 禁用, 不可登录; 密码为 admin123 的哈希, 反正登录不了)
INSERT IGNORE INTO sys_user (id, username, password, phone, real_name, role_id, status) VALUES
(100, 'ai_reviewer', '$2a$10$awNsz6ElecxeHt9ASDN1TeuCJNJHSq.GK3pW5FAfsZ7KXfEYgcacm', NULL, 'AI Auto Reviewer', 1, 0);

-- 4. 权限码 (管理员专属)
INSERT IGNORE INTO sys_permission (id, perm_name, perm_code, perm_path, parent_id, perm_type, sort_order) VALUES
(17, 'AI Settings', 'ai:settings', '/api/ai/settings', 5, 'button', 3),
(18, 'AI Logs',     'ai:logs',     '/api/ai/logs',     5, 'button', 4);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (1,17), (1,18);
