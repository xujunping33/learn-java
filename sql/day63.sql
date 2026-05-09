-- Day63：Week9 周整合入口（升级 + 事务 + EXPLAIN）
--
-- 运行前强烈建议先做 Day62 备份（能恢复才算备份）：
-- - 参考：sql/week9_backup_restore.md
--
-- 说明：
-- - schema_upgrade.sql 是“升级脚本”，包含 department + 数据清洗 + 外键（Day57/58）
-- - tx_isolation.sql 里 Day60 是“双会话步骤”，不会在这里自动跑出隔离级别现象
-- - explain_index.sql 里会创建一个联合索引（幂等判断存在则跳过）

SOURCE /home/xjp/xjp/code/learn/java/sql/week9_schema_upgrade.sql;
SOURCE /home/xjp/xjp/code/learn/java/sql/week9_tx_isolation.sql;
SOURCE /home/xjp/xjp/code/learn/java/sql/week9_explain_index.sql;

