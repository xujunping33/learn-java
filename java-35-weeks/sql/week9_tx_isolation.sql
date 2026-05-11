-- Week9 事务与隔离级别演示脚本
-- Day59：事务基础（BEGIN / COMMIT / ROLLBACK）
--
-- 建议：
-- - 先跑 Day57/Day58（结构升级与外键）不影响本脚本
-- - Day60 再在本文件追加“双会话隔离级别演示步骤”

USE learn_java;

-- 0) 准备：账户表
CREATE TABLE IF NOT EXISTS account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- 1) 初始化数据（幂等：存在就更新余额到指定值，便于重复跑）
INSERT INTO account (name, balance) VALUES
  ('alice', 1000.00),
  ('bob', 500.00)
ON DUPLICATE KEY UPDATE balance = VALUES(balance);

SELECT * FROM account ORDER BY id;

-- ============================================================
-- A. 成功转账：BEGIN -> 两次 UPDATE -> COMMIT
-- ============================================================

START TRANSACTION;

-- alice -> bob 转 200
UPDATE account SET balance = balance - 200.00 WHERE name = 'alice';
UPDATE account SET balance = balance + 200.00 WHERE name = 'bob';

COMMIT;

SELECT 'after commit' AS stage, * FROM account ORDER BY id;

-- ============================================================
-- B. 失败回滚：BEGIN -> 扣款 -> 人为制造错误 -> ROLLBACK
-- ============================================================
-- 说明：第二条 UPDATE 故意写错表名，触发错误后你手动执行 ROLLBACK。
-- 这样能直观看到：扣款那一步不会“半成功”留在库里。

START TRANSACTION;

-- alice -> bob 转 300（先扣款）
UPDATE account SET balance = balance - 300.00 WHERE name = 'alice';

-- 这里故意制造错误（表名写错）：执行会报错
-- 你看到报错后，立刻执行：ROLLBACK;
UPDATE account_not_exists SET balance = balance + 300.00 WHERE name = 'bob';

-- 如果你不小心把上面错误修好了并执行成功，那么这里的 ROLLBACK 就不会再起作用。
ROLLBACK;

SELECT 'after rollback' AS stage, * FROM account ORDER BY id;

-- ============================================================
-- Day60：隔离级别演示（双会话：Session A / Session B）
-- ============================================================
-- 目标：观察“不可重复读”的差异（RC vs RR）
--
-- 使用方式：
-- - 打开两个 MySQL 客户端窗口（两个终端/两个连接）
-- - 下面的步骤，按顺序在 A / B 中执行
--
-- 先看当前隔离级别（全局/当前会话）
--   SELECT @@global.transaction_isolation, @@session.transaction_isolation;
--
-- 提示：
-- - MySQL 8 常用变量：transaction_isolation
-- - InnoDB 常见默认：REPEATABLE READ（不同环境可能不同，以你机器为准）

-- Step 0（两边都执行）：准备基准数据，便于反复演示
-- UPDATE account SET balance = CASE name WHEN 'alice' THEN 1000.00 WHEN 'bob' THEN 500.00 END
-- WHERE name IN ('alice','bob');

-- ------------------------------------------------------------
-- 场景 1：READ COMMITTED 下的“不可重复读”
-- 现象：同一事务里两次 SELECT 可能读到不同结果
-- ------------------------------------------------------------
-- Session A：
-- SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
-- START TRANSACTION;
-- SELECT balance FROM account WHERE name = 'alice';   -- 记下值 v1（例如 1000）
--
-- Session B：
-- SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
-- START TRANSACTION;
-- UPDATE account SET balance = balance - 100.00 WHERE name = 'alice';
-- COMMIT;
--
-- Session A（仍在同一事务内）：
-- SELECT balance FROM account WHERE name = 'alice';   -- 可能变成 v2（例如 900）
-- COMMIT;

-- ------------------------------------------------------------
-- 场景 2：REPEATABLE READ 下（对比）
-- 现象：同一事务里的“快照读”通常保持一致（两次 SELECT 看到同一个值）
-- ------------------------------------------------------------
-- Session A：
-- SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;
-- START TRANSACTION;
-- SELECT balance FROM account WHERE name = 'alice';   -- 记下值 v1
--
-- Session B：
-- SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;
-- START TRANSACTION;
-- UPDATE account SET balance = balance - 100.00 WHERE name = 'alice';
-- COMMIT;
--
-- Session A（仍在同一事务内）：
-- SELECT balance FROM account WHERE name = 'alice';   -- 通常仍是 v1
-- COMMIT;

-- 你需要记录的结论（写入 W9-notes.md）：
-- - 你机器的默认隔离级别是什么？
-- - RC vs RR：两次 SELECT 的结果是否变化？

