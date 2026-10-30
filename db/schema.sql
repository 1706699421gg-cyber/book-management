-- =========================================================
-- 图书管理系统数据库初始化脚本
-- 数据库: book_management   字符集: utf8mb4
-- 使用方式(任选其一):
--   1) 命令行: mysql -uroot -p < db/schema.sql
--   2) 客户端: 在 Navicat / IDEA Database 面板中直接执行本文件
-- =========================================================

CREATE DATABASE IF NOT EXISTS book_management
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE book_management;

-- ----------------------------
-- 删除旧表(注意顺序: 先子表 t_book 再父表 t_category)
-- ----------------------------
DROP TABLE IF EXISTS t_book;
DROP TABLE IF EXISTS t_category;
DROP TABLE IF EXISTS t_user;

-- ----------------------------
-- 图书分类表
-- ----------------------------
CREATE TABLE t_category (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)  NOT NULL                COMMENT '分类名称',
    description VARCHAR(255) DEFAULT NULL            COMMENT '分类描述',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT = '图书分类表';

-- ----------------------------
-- 图书表
-- ----------------------------
CREATE TABLE t_book (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name         VARCHAR(100)  NOT NULL                COMMENT '书名',
    author       VARCHAR(50)   DEFAULT NULL            COMMENT '作者',
    isbn         VARCHAR(20)   DEFAULT NULL            COMMENT 'ISBN编号',
    category_id  BIGINT        DEFAULT NULL            COMMENT '分类ID(关联t_category.id)',
    price        DECIMAL(10,2) DEFAULT NULL            COMMENT '价格(元)',
    stock        INT           NOT NULL DEFAULT 0      COMMENT '库存',
    publish_date DATE          DEFAULT NULL            COMMENT '出版日期',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_book_name (name),
    KEY idx_book_category_id (category_id)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT = '图书表';

-- ----------------------------
-- 用户表(登录 + 角色权限)
-- 默认账号由后端首次启动时自动创建:
--   admin  / admin123  管理员(全部权限)
--   reader / user123   普通用户(只读)
-- ----------------------------
CREATE TABLE t_user (
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

-- ----------------------------
-- 初始数据: 分类(6条)
-- ----------------------------
INSERT INTO t_category (name, description) VALUES
('文学小说',   '中外经典小说、散文与文学作品'),
('计算机技术', '编程语言、框架、数据库与软件工程'),
('历史传记',   '历史研究、人物传记与纪实文学'),
('经济管理',   '经济学、金融、管理与商业思维'),
('少儿读物',   '儿童启蒙、绘本与青少年读物'),
('艺术设计',   '设计理论、绘画技法与艺术史');

-- ----------------------------
-- 初始数据: 图书(20条, 其中2条库存为0用于验证前端标红)
-- ----------------------------
INSERT INTO t_book (name, author, isbn, category_id, price, stock, publish_date) VALUES
('三体',               '刘慈欣',           '9787536692930', 1, 39.50,  25, '2008-01-01'),
('活着',               '余华',             '9787506365437', 1, 28.00,  18, '1993-11-01'),
('平凡的世界',         '路遥',             '9787020008735', 1, 108.00, 12, '1986-12-01'),
('百年孤独',           '加西亚·马尔克斯',  '9787544253994', 1, 55.00,  8,  '1967-05-30'),
('深入理解Java虚拟机', '周志明',           '9787111641247', 2, 129.00, 15, '2019-12-01'),
('Java核心技术 卷I',   'Cay S. Horstmann', '9787115558422', 2, 149.00, 10, '2022-01-01'),
('Spring实战',         'Craig Walls',      '9787115417305', 2, 89.00,  0,  '2016-04-01'),
('算法导论',           'Thomas H. Cormen', '9787111407010', 2, 128.00, 6,  '2012-12-01'),
('MySQL必知必会',      'Ben Forta',        '9787115192686', 2, 49.00,  0,  '2009-01-01'),
('人类简史',           '尤瓦尔·赫拉利',    '9787508647357', 3, 68.00,  20, '2014-11-01'),
('明朝那些事儿',       '当年明月',         '9787807361986', 3, 358.20, 5,  '2009-04-01'),
('苏东坡传',           '林语堂',           '9787540465897', 3, 45.00,  9,  '2017-06-01'),
('史记(精装本)',       '司马迁',           '9787101003048', 3, 58.00,  4,  '1982-11-01'),
('穷查理宝典',         '彼得·考夫曼',      '9787508683326', 4, 88.00,  7,  '2010-01-01'),
('经济学原理',         '曼昆',             '9787301256501', 4, 128.00, 11, '2015-03-01'),
('管理学',             '斯蒂芬·罗宾斯',    '9787300229166', 4, 79.00,  3,  '2017-01-01'),
('小王子',             '圣埃克苏佩里',     '9787020042494', 5, 22.00,  30, '1942-04-06'),
('夏洛的网',           'E.B.怀特',         '9787532759826', 5, 23.00,  14, '1952-09-15'),
('艺术的故事',         '贡布里希',         '9787559606290', 6, 168.00, 2,  '2016-01-01'),
('写给大家的设计书',   'Robin Williams',   '9787115388871', 6, 59.00,  6,  '2016-02-01');

-- ----------------------------
-- 执行后自检
-- ----------------------------
SELECT COUNT(*) AS category_count FROM t_category;   -- 预期 6
SELECT COUNT(*) AS book_count     FROM t_book;       -- 预期 20
SELECT COUNT(*) AS user_count     FROM t_user;       -- 预期 0(后端首次启动后自动创建 2 个默认账号)
SELECT name, stock FROM t_book WHERE stock = 0;      -- 预期 Spring实战 / MySQL必知必会
