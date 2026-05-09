-- Day56：周整合入口
-- 目标：一键执行 Week8 的三份交付脚本：
-- - week8_schema.sql
-- - week8_crud.sql
-- - week8_functions.sql
--
-- 注意：
-- - week8_schema.sql 会 DROP 并重建表（会清空数据）
-- - 如果你不想清空数据：请不要执行 schema，只执行 crud/functions

SOURCE /home/xjp/xjp/code/learn/java/sql/week8_schema.sql;
SOURCE /home/xjp/xjp/code/learn/java/sql/week8_crud.sql;
SOURCE /home/xjp/xjp/code/learn/java/sql/week8_functions.sql;

