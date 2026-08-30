-- ===================================================
-- 增量迁移: 来访原因"其他"自由填写
-- 在 appointment 表新增 reason_detail 字段，存储申请者自填的具体原因
-- ===================================================

USE visitor_system;

ALTER TABLE appointment
    ADD COLUMN reason_detail VARCHAR(255) DEFAULT NULL COMMENT '来访原因详情(选择"其他"时自由填写)' AFTER visit_reason;
