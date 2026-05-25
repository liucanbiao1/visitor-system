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
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS access_log;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS visitor;

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
(1, '系统管理员', 'ROLE_ADMIN', '拥有系统全部权限，可管理用户、角色及所有业务模块'),
(2, '普通管理员', 'ROLE_USER',  '日常访客管理权限，可查看及操作访客和预约模块');

-- ===================================================
-- 初始数据: 权限 (两级树形菜单)
-- ===================================================
-- 一级菜单 (parent_id = 0)
INSERT INTO sys_permission (id, perm_name, perm_code, perm_path, parent_id, perm_type, sort_order, icon) VALUES
(1,  '首页概览',   'dashboard',     '/dashboard',        0, 'menu', 1, 'DataAnalysis'),
(2,  '用户管理',   'user',          '/user',             0, 'menu', 2, 'User'),
(3,  '角色管理',   'role',          '/role',             0, 'menu', 3, 'Switch'),
(4,  '访客管理',   'visitor',       '/visitor',          0, 'menu', 4, 'Avatar'),
(5,  '预约管理',   'appointment',   '/appointment',      0, 'menu', 5, 'Calendar'),
(6,  '门禁记录',   'access-log',    '/access-log',       0, 'menu', 6, 'Monitor');

-- 二级菜单/按钮 (parent_id = 一级菜单ID)
INSERT INTO sys_permission (id, perm_name, perm_code, perm_path, parent_id, perm_type, sort_order, icon) VALUES
(7,  '用户查询',   'user:list',     '/api/user/list',     2, 'button', 1, NULL),
(8,  '用户新增',   'user:add',      '/api/user/add',      2, 'button', 2, NULL),
(9,  '用户编辑',   'user:edit',     '/api/user/edit',     2, 'button', 3, NULL),
(10, '用户删除',   'user:delete',   '/api/user/delete',   2, 'button', 4, NULL),
(11, '访客查询',   'visitor:list',  '/api/visitor/list',  4, 'button', 1, NULL),
(12, '访客新增',   'visitor:add',   '/api/visitor/add',   4, 'button', 2, NULL),
(13, '访客编辑',   'visitor:edit',  '/api/visitor/edit',  4, 'button', 3, NULL),
(14, '预约审批',   'appointment:review', '/api/appointment/review', 5, 'button', 1, NULL),
(15, '预约查询',   'appointment:list',   '/api/appointment/list',   5, 'button', 2, NULL),
(16, '门禁查询',   'access-log:list',    '/api/access-log/list',    6, 'button', 1, NULL);

-- ===================================================
-- 初始数据: 管理员用户 (密码: admin123, BCrypt加密)
-- ===================================================
INSERT INTO sys_user (id, username, password, phone, real_name, role_id, status) VALUES
(1, 'admin', '$2a$10$awNsz6ElecxeHt9ASDN1TeuCJNJHSq.GK3pW5FAfsZ7KXfEYgcacm', '13800000000', '系统管理员', 1, 1);

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
