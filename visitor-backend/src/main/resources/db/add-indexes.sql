-- ===================================================
-- 增量索引优化脚本（适用于已有数据库）
-- 执行前请确保已选择正确的数据库: USE visitor_system;
-- ===================================================

-- visitor 表: name 字段用于关键词搜索
ALTER TABLE visitor ADD INDEX idx_name (name);

-- appointment 表: 按状态+创建时间的复合索引（审核列表高频查询）
ALTER TABLE appointment ADD INDEX idx_status_create (status, create_time);

-- access_log 表: 入校/离校时间复合索引（超时查询、在校查询）
ALTER TABLE access_log ADD INDEX idx_entry_exit (entry_time, exit_time);
