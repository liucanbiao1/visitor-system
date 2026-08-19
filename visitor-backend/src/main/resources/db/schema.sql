-- ===================================================
-- 校园自动访客管理系统 - 数据库初始化脚本
-- Database: MySQL 8.0+
-- Encoding: utf8mb4
-- ===================================================

CREATE DATABASE IF NOT EXISTS visitor_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE visitor_system;

-- ===================================================
-- 1. 角色表
-- ===================================================
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS access_log;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS visitor;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_role (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '角色ID',
    role_name   VARCHAR(50)     NOT NULL                 COMMENT '角色名称',
    role_code   VARCHAR(50)     NOT NULL                 COMMENT '角色编码',
    description VARCHAR(255)    DEFAULT NULL             COMMENT '角色描述',
    create_time DATETIME        NOT NULL DEFAULT NOW()  COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ===================================================
-- 2. 用户表
-- ===================================================
CREATE TABLE sys_user (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    username    VARCHAR(50)     NOT NULL                 COMMENT '用户名',
    password    VARCHAR(255)    NOT NULL                 COMMENT '加密密码',
    phone       VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    real_name   VARCHAR(50)     DEFAULT NULL             COMMENT '真实姓名',
    role_id     BIGINT          NOT NULL                 COMMENT '角色ID',
    status      TINYINT         NOT NULL DEFAULT 1       COMMENT '状态 1启用 0禁用',
    create_time DATETIME        NOT NULL DEFAULT NOW()  COMMENT '创建时间',
    update_time DATETIME        DEFAULT NULL ON UPDATE NOW() COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role_id (role_id),
    KEY idx_phone (phone),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ===================================================
-- 3. 权限表
-- ===================================================
CREATE TABLE sys_permission (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '权限ID',
    perm_name   VARCHAR(100)    NOT NULL                 COMMENT '权限名称',
    perm_code   VARCHAR(100)    NOT NULL                 COMMENT '权限标识',
    perm_path   VARCHAR(255)    DEFAULT NULL             COMMENT '权限路径/URL',
    parent_id   BIGINT          NOT NULL DEFAULT 0       COMMENT '父权限ID',
    perm_type   VARCHAR(20)     NOT NULL DEFAULT 'menu'  COMMENT '类型: menu/button/api',
    sort_order  INT             NOT NULL DEFAULT 0       COMMENT '排序',
    icon        VARCHAR(100)    DEFAULT NULL             COMMENT '菜单图标',
    create_time DATETIME        NOT NULL DEFAULT NOW()  COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ===================================================
-- 4. 角色-权限关联表
-- ===================================================
CREATE TABLE sys_role_permission (
    id              BIGINT  NOT NULL AUTO_INCREMENT  COMMENT '主键',
    role_id         BIGINT  NOT NULL                 COMMENT '角色ID',
    permission_id   BIGINT  NOT NULL                 COMMENT '权限ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES sys_permission (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ===================================================
-- 5. 访客表
-- ===================================================
CREATE TABLE visitor (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '访客ID',
    name        VARCHAR(50)     NOT NULL                 COMMENT '姓名',
    phone       VARCHAR(20)     NOT NULL                 COMMENT '手机号',
    id_card     VARCHAR(18)     NOT NULL                 COMMENT '身份证号',
    gender      TINYINT         NOT NULL DEFAULT 0       COMMENT '性别 0未知 1男 2女',
    photo_url   VARCHAR(255)    DEFAULT NULL             COMMENT '照片路径',
    status      TINYINT         NOT NULL DEFAULT 1       COMMENT '状态 1正常 0黑名单',
    create_time DATETIME        NOT NULL DEFAULT NOW()  COMMENT '创建时间',
    update_time DATETIME        DEFAULT NULL ON UPDATE NOW() COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_phone (phone),
    KEY idx_id_card (id_card),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客表';

-- ===================================================
-- 6. 预约表
-- ===================================================
CREATE TABLE appointment (
    id                BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '预约ID',
    visitor_id        BIGINT        NOT NULL                 COMMENT '访客ID',
    appointment_time  DATETIME      NOT NULL                 COMMENT '预约访问时间',
    visit_reason      VARCHAR(255)  NOT NULL                 COMMENT '来访原因',
    host_name         VARCHAR(50)   NOT NULL                 COMMENT '访问对象',
    host_dept         VARCHAR(100)  DEFAULT NULL             COMMENT '被访部门',
    status            TINYINT       NOT NULL DEFAULT 0       COMMENT '0待审核 1已通过 2已拒绝 3已完成 4已取消',
    reject_reason     VARCHAR(255)  DEFAULT NULL             COMMENT '拒绝原因',
    reviewer_id       BIGINT        DEFAULT NULL             COMMENT '审核人ID',
    review_time       DATETIME      DEFAULT NULL             COMMENT '审核时间',
    create_time       DATETIME      NOT NULL DEFAULT NOW()  COMMENT '创建时间',
    update_time       DATETIME      DEFAULT NULL ON UPDATE NOW() COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_visitor_id (visitor_id),
    KEY idx_status (status),
    KEY idx_appointment_time (appointment_time),
    KEY idx_host_name (host_name),
    KEY idx_status_create (status, create_time),
    CONSTRAINT fk_appt_visitor FOREIGN KEY (visitor_id) REFERENCES visitor (id),
    CONSTRAINT fk_appt_reviewer FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ===================================================
-- 7. 门禁记录表
-- ===================================================
CREATE TABLE access_log (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    visitor_id      BIGINT        NOT NULL                 COMMENT '访客ID',
    appointment_id  BIGINT        DEFAULT NULL             COMMENT '关联预约ID',
    entry_time      DATETIME      DEFAULT NULL             COMMENT '进入时间',
    exit_time       DATETIME      DEFAULT NULL             COMMENT '离开时间',
    access_status   TINYINT       NOT NULL DEFAULT 1       COMMENT '1通行成功 0通行失败',
    device_name     VARCHAR(100)  DEFAULT NULL             COMMENT '门禁设备名称',
    fail_reason     VARCHAR(255)  DEFAULT NULL             COMMENT '失败原因',
    create_time     DATETIME      NOT NULL DEFAULT NOW()  COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_visitor_id (visitor_id),
    KEY idx_appointment_id (appointment_id),
    KEY idx_entry_time (entry_time),
    KEY idx_entry_exit (entry_time, exit_time),
    CONSTRAINT fk_log_visitor FOREIGN KEY (visitor_id) REFERENCES visitor (id),
    CONSTRAINT fk_log_appt FOREIGN KEY (appointment_id) REFERENCES appointment (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁记录表';

-- ===================================================
-- 初始数据: 角色
-- ===================================================
INSERT INTO sys_role (id, role_name, role_code, description) VALUES
(1, 'System Admin', 'ROLE_ADMIN', 'Full system access, manage users, roles and all business modules'),
(2, 'Operator',     'ROLE_USER',  'Daily visitor management, view and operate visitor and appointment modules');

-- ===================================================
-- 初始数据: 权限 (两级树形菜单)
-- ===================================================
-- 一级菜单 (parent_id = 0)
INSERT INTO sys_permission (id, perm_name, perm_code, perm_path, parent_id, perm_type, sort_order, icon) VALUES
(1,  'Dashboard',            'dashboard',     '/dashboard',        0, 'menu', 1, 'DataAnalysis'),
(2,  'User Management',      'user',          '/user',             0, 'menu', 2, 'User'),
(3,  'Role Management',      'role',          '/role',             0, 'menu', 3, 'Switch'),
(4,  'Visitor Management',   'visitor',       '/visitor',          0, 'menu', 4, 'Avatar'),
(5,  'Appointment Mgmt',     'appointment',   '/appointment',      0, 'menu', 5, 'Calendar'),
(6,  'Access Log',           'access-log',    '/access-log',       0, 'menu', 6, 'Monitor');

-- 二级菜单/按钮 (parent_id = 一级菜单ID)
INSERT INTO sys_permission (id, perm_name, perm_code, perm_path, parent_id, perm_type, sort_order, icon) VALUES
(7,  'User Query',            'user:list',     '/api/user/list',     2, 'button', 1, NULL),
(8,  'User Add',              'user:add',      '/api/user/add',      2, 'button', 2, NULL),
(9,  'User Edit',             'user:edit',     '/api/user/edit',     2, 'button', 3, NULL),
(10, 'User Delete',           'user:delete',   '/api/user/delete',   2, 'button', 4, NULL),
(11, 'Visitor Query',         'visitor:list',  '/api/visitor/list',  4, 'button', 1, NULL),
(12, 'Visitor Add',           'visitor:add',   '/api/visitor/add',   4, 'button', 2, NULL),
(13, 'Visitor Edit',          'visitor:edit',  '/api/visitor/edit',  4, 'button', 3, NULL),
(14, 'Appointment Review',    'appointment:review', '/api/appointment/review', 5, 'button', 1, NULL),
(15, 'Appointment Query',     'appointment:list',   '/api/appointment/list',   5, 'button', 2, NULL),
(16, 'Access Log Query',      'access-log:list',    '/api/access-log/list',    6, 'button', 1, NULL);

-- ===================================================
-- 初始数据: 管理员用户 (密码: admin123, BCrypt加密)
-- ===================================================
INSERT INTO sys_user (id, username, password, phone, real_name, role_id, status) VALUES
(1, 'admin', '$2a$10$awNsz6ElecxeHt9ASDN1TeuCJNJHSq.GK3pW5FAfsZ7KXfEYgcacm', '13800000000', 'System Admin', 1, 1);

-- ===================================================
-- 初始数据: 角色-权限映射
-- ===================================================
-- 管理员 (role_id=1): 拥有全部 16 条权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1,1), (1,2), (1,3), (1,4), (1,5), (1,6), (1,7), (1,8),
(1,9), (1,10), (1,11), (1,12), (1,13), (1,14), (1,15), (1,16);

-- 普通管理员 (role_id=2): 仅有首页、访客管理、预约管理的查看权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2,1), (2,4), (2,5), (2,11), (2,15);

-- ===================================================
-- 初始数据: 额外管理员用户 (密码: admin123)
-- ===================================================
INSERT INTO sys_user (id, username, password, phone, real_name, role_id, status) VALUES
(2, 'zhangsan', '$2a$10$awNsz6ElecxeHt9ASDN1TeuCJNJHSq.GK3pW5FAfsZ7KXfEYgcacm', '13900000001', 'Zhang San', 2, 1),
(3, 'lisi',     '$2a$10$awNsz6ElecxeHt9ASDN1TeuCJNJHSq.GK3pW5FAfsZ7KXfEYgcacm', '13900000002', 'Li Si',     2, 1);

-- ===================================================
-- 初始数据: 访客 (12 位)
-- ===================================================
INSERT INTO visitor (id, name, phone, id_card, gender, status, create_time) VALUES
(1,  'James Wang',   '13811110001', '110101199805061234', 1, 1, '2026-05-01 09:00:00'),
(2,  'Emily Li',     '13811110002', '110102199203152345', 2, 1, '2026-05-03 10:30:00'),
(3,  'Michael Zhang','13811110003', '320501198710204567', 1, 1, '2026-05-05 14:00:00'),
(4,  'Lily Zhao',    '13811110004', '330102200001016789', 2, 1, '2026-05-08 08:15:00'),
(5,  'Jack Chen',    '13811110005', '440301199511128901', 1, 0, '2026-05-10 16:45:00'),
(6,  'Sophia Liu',   '13811110006', '500105199808230123', 2, 1, '2026-05-12 11:20:00'),
(7,  'David Huang',  '13811110007', '610102199107153456', 1, 1, '2026-05-15 09:30:00'),
(8,  'Grace Zhou',   '13811110008', '350103200203177890', 2, 1, '2026-05-16 13:00:00'),
(9,  'Robert Wu',    '13811110009', '210201199605049012', 1, 1, '2026-05-18 07:50:00'),
(10, 'Anna Sun',     '13811110010', '120104199903271234', 2, 1, '2026-05-20 10:10:00'),
(11, 'Tom Qian',     '13811110011', '510107198812121567', 1, 1, '2026-05-22 15:00:00'),
(12, 'Mia Zheng',    '13811110012', '420112200103089876', 2, 1, '2026-05-24 08:40:00');

-- ===================================================
-- 初始数据: 预约 (20 条，覆盖各种状态)
-- ===================================================
INSERT INTO appointment (id, visitor_id, appointment_time, visit_reason, host_name, host_dept, status, reject_reason, reviewer_id, review_time, create_time) VALUES
-- Approved
(1,  1,  '2026-05-20 10:00:00', 'Campus Tour',              'Prof. Zhang',     'Computer Science',  1, NULL,   1, '2026-05-19 15:00:00', '2026-05-18 09:00:00'),
(2,  2,  '2026-05-22 14:00:00', 'Academic Exchange',         'Prof. Li',        'Mathematics Dept',  1, NULL,   1, '2026-05-21 10:00:00', '2026-05-20 09:00:00'),
(3,  3,  '2026-05-25 09:00:00', 'Job Interview',             'Director Wang',   'HR Department',     1, NULL,   2, '2026-05-24 16:00:00', '2026-05-23 11:00:00'),
(4,  4,  '2026-05-26 11:00:00', 'Lab Open Day',              'Prof. Zhao',      'Physics Dept',      1, NULL,   1, '2026-05-25 14:00:00', '2026-05-24 08:00:00'),
(5,  6,  '2026-05-28 15:00:00', 'Book Donation',             'Curator Liu',     'Library',           1, NULL,   2, '2026-05-27 09:00:00', '2026-05-26 10:00:00'),
(6,  7,  '2026-05-29 08:30:00', 'Equipment Maintenance',     'Engineer Chen',   'Logistics',         1, NULL,   1, '2026-05-28 11:00:00', '2026-05-27 08:00:00'),
(7,  8,  '2026-06-01 14:00:00', 'Graduation Ceremony',       'Director Zhou',   'Academic Affairs',  1, NULL,   1, '2026-05-28 16:00:00', '2026-05-28 07:00:00'),
(8,  9,  '2026-06-03 10:00:00', 'Research Collaboration',    'Dean Wu',         'Computer Science',  1, NULL,   2, '2026-05-28 14:00:00', '2026-05-28 10:00:00'),
-- Rejected
(9,  10, '2026-05-15 09:00:00', 'Product Sales',             'Prof. Sun',       'Management School', 2, 'Unclear visit purpose, sales visits not permitted', 1, '2026-05-14 10:00:00', '2026-05-13 09:00:00'),
(10, 11, '2026-05-17 11:00:00', 'Credit Card Promotion',     'Director Qian',   'Finance Dept',      2, 'Unauthorized commercial promotion on campus',       2, '2026-05-16 14:00:00', '2026-05-15 10:00:00'),
-- Pending
(11, 12, '2026-06-05 10:00:00', 'Alumni Visit',              'Director Zheng',  'Alumni Office',     0, NULL,   NULL, NULL,                  '2026-05-28 14:00:00'),
(12, 4,  '2026-06-08 15:00:00', 'Second Lab Visit',          'Prof. Zhao',      'Physics Dept',      0, NULL,   NULL, NULL,                  '2026-05-28 16:00:00'),
(13, 5,  '2026-05-19 10:00:00', 'Noise Complaint',           'Director Li',     'Logistics',         2, 'Visitor is blacklisted',                           1, '2026-05-18 10:00:00', '2026-05-17 09:00:00'),
-- Completed
(14, 1,  '2026-05-10 09:00:00', 'Volunteer Activity',        'Prof. Zhang',     'Computer Science',  3, NULL,   1, '2026-05-09 15:00:00', '2026-05-08 09:00:00'),
(15, 2,  '2026-05-12 14:00:00', 'Parent-Teacher Meeting',    'Prof. Li',        'Mathematics Dept',  3, NULL,   2, '2026-05-11 16:00:00', '2026-05-10 11:00:00'),
(16, 3,  '2026-05-14 11:00:00', 'Package Pickup',            'Director Wang',   'HR Department',     3, NULL,   1, '2026-05-13 14:00:00', '2026-05-12 08:00:00'),
-- Cancelled
(17, 6,  '2026-05-08 08:00:00', 'Test Cancellation',         'Curator Liu',     'Library',           4, NULL,   NULL, NULL,                  '2026-05-07 09:00:00'),
(18, 7,  '2026-05-09 16:00:00', 'Cancelled Due to Weather',  'Engineer Chen',   'Logistics',         4, NULL,   NULL, NULL,                  '2026-05-08 10:00:00'),
-- More Approved
(19, 7,  '2026-05-30 09:00:00', 'Network Equipment Check',   'Engineer Chen',   'Logistics',         1, NULL,   1, '2026-05-29 14:00:00', '2026-05-28 15:00:00'),
(20, 9,  '2026-05-30 14:00:00', 'University-Enterprise MOU', 'Dean Wu',         'Computer Science',  1, NULL,   1, '2026-05-29 15:00:00', '2026-05-28 16:00:00');

-- ===================================================
-- 初始数据: 门禁记录 (20 条)
-- ===================================================
INSERT INTO access_log (id, visitor_id, appointment_id, entry_time, exit_time, access_status, device_name, create_time) VALUES
(1,  1,  14, '2026-05-10 08:55:00', '2026-05-10 11:30:00', 1, 'East Gate A01', '2026-05-10 08:55:00'),
(2,  2,  15, '2026-05-12 13:45:00', '2026-05-12 16:20:00', 1, 'South Gate B02', '2026-05-12 13:45:00'),
(3,  3,  16, '2026-05-14 10:50:00', '2026-05-14 12:00:00', 1, 'East Gate A02', '2026-05-14 10:50:00'),
(4,  1,  1,  '2026-05-20 09:50:00', '2026-05-20 12:10:00', 1, 'East Gate A01', '2026-05-20 09:50:00'),
(5,  2,  2,  '2026-05-22 13:50:00', '2026-05-22 17:00:00', 1, 'South Gate B01', '2026-05-22 13:50:00'),
(6,  3,  3,  '2026-05-25 08:45:00', '2026-05-25 10:30:00', 1, 'East Gate A03', '2026-05-25 08:45:00'),
(7,  4,  4,  '2026-05-26 10:50:00', '2026-05-26 15:00:00', 1, 'West Gate C01', '2026-05-26 10:50:00'),
(8,  6,  5,  '2026-05-28 14:45:00', '2026-05-28 16:30:00', 1, 'East Gate A01', '2026-05-28 14:45:00'),
(9,  7,  6,  '2026-05-29 08:20:00', NULL,                    1, 'South Gate B02', '2026-05-29 08:20:00'),
(10, 8,  7,  NULL,                    NULL,                    1, 'West Gate C02', '2026-05-28 07:00:00'),
(11, 5,  NULL, '2026-05-18 07:30:00', NULL,                  0, 'East Gate A01', '2026-05-18 07:30:00'),
(12, 5,  NULL, '2026-05-19 08:10:00', NULL,                  0, 'South Gate B01', '2026-05-19 08:10:00'),
(13, 7,  19, '2026-05-30 08:50:00', '2026-05-30 11:00:00',  1, 'East Gate A01', '2026-05-30 08:50:00'),
(14, 9,  20, '2026-05-30 13:45:00', '2026-05-30 17:00:00',  1, 'South Gate B02', '2026-05-30 13:45:00'),
(15, 10, NULL, '2026-05-13 08:30:00', NULL,                  0, 'East Gate A02', '2026-05-13 08:30:00'),
(16, 4,  12, '2026-06-08 14:50:00', NULL,                    1, 'West Gate C01', '2026-06-08 14:50:00'),
(17, 11, NULL, '2026-05-16 07:20:00', NULL,                  0, 'South Gate B03', '2026-05-16 07:20:00'),
(18, 12, 11, '2026-06-05 09:50:00', '2026-06-05 12:30:00',  1, 'East Gate A01', '2026-06-05 09:50:00'),
(19, 8,  7,  '2026-06-01 13:55:00', '2026-06-01 16:00:00',  1, 'West Gate C01', '2026-06-01 13:55:00'),
(20, 9,  8,  '2026-06-03 09:50:00', NULL,                    1, 'East Gate A03', '2026-06-03 09:50:00');
