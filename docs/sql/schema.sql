-- 权限管理模块 建表 DDL（MySQL 8 / InnoDB / utf8mb4）
-- 来源：3.3-角色管理-技术方案 + 3.4-权限数据初始化-技术方案

CREATE DATABASE IF NOT EXISTS `permission` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `permission`;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id`           BIGINT       NOT NULL COMMENT '主键ID（雪花）',
  `username`     VARCHAR(50)  NOT NULL COMMENT '用户名（唯一，存小写）',
  `password`     VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 密文）',
  `nickname`     VARCHAR(50)  NOT NULL COMMENT '姓名',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `force_change` TINYINT      NOT NULL DEFAULT 0 COMMENT '强制改密：0-否 1-是（首次登录/重置后置1）',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_user_nickname` (`nickname`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id`          BIGINT       NOT NULL COMMENT '主键ID（雪花）',
  `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名',
  `role_code`   VARCHAR(50)  NOT NULL COMMENT '角色编码（唯一）',
  `description` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_role_name` (`role_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

-- 权限表（系统预置，无维护接口）
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id`              BIGINT       NOT NULL COMMENT '主键ID（雪花）',
  `permission_name` VARCHAR(50)  NOT NULL COMMENT '权限名',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码（唯一）',
  `parent_id`       BIGINT       NOT NULL DEFAULT 0 COMMENT '父级ID，0=根',
  `route`           VARCHAR(200) NOT NULL DEFAULT '' COMMENT '路由/URL，目录为空',
  `sort`            INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `type`            TINYINT      NOT NULL COMMENT '类型：1-目录 2-页面',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_permission_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '权限表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id`          BIGINT   NOT NULL COMMENT '主键ID（雪花）',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
  `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户-角色关联表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id`            BIGINT   NOT NULL COMMENT '主键ID（雪花）',
  `role_id`       BIGINT   NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT   NOT NULL COMMENT '权限ID',
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色-权限关联表';
