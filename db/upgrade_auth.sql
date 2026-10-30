-- =========================================================
-- 增量升级脚本: 新增登录 / 角色权限相关表
-- 适用于已经初始化过 book_management 数据库(已有图书数据)的情况
-- 由 start.ps1 自动检测并执行; 也可手动执行:
--   mysql -uroot -p < db/upgrade_auth.sql
-- 说明: 本脚本不会删除或修改 t_book / t_category 中的现有数据
-- =========================================================

USE book_management;

-- ----------------------------
-- 用户表(登录 + 角色权限)
-- 默认账号由后端首次启动时自动创建:
--   admin  / admin123  管理员(全部权限)
--   reader / user123   普通用户(只读)
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL                COMMENT '登录账号',
    password    VARCHAR(255) NOT NULL                COMMENT '密码(加盐哈希, 由后端生成)',
    real_name   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名/昵称',
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN 管理员 / USER 普通用户',
    status      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1 启用 / 0 禁用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

SELECT COUNT(*) AS user_count FROM t_user;
