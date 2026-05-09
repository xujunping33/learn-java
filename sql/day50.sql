-- Day50：安装/连接 MySQL + 最小建库建表练习
-- 使用方式（任选其一）：
-- 1) 进入 mysql 客户端后：source /path/to/sql/day50.sql;
-- 2) 命令行：mysql -u root -p < sql/day50.sql

-- 1) 最简单 SQL：确认能执行
SELECT VERSION();
SHOW DATABASES;

-- 2) 建库：learn_java（字符集推荐 utf8mb4）
CREATE DATABASE IF NOT EXISTS learn_java
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE learn_java;

-- 3) 建表：t_user（最小字段：id、username、created_at）
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- 4) 简单验证：插入 + 查询
INSERT INTO t_user (username) VALUES ('alice'), ('bob');

SELECT * FROM t_user ORDER BY id;

