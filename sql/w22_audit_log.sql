-- Week22 Day149：审计独立提交（REQUIRES_NEW），放在 learn_java 库执行一次即可

USE learn_java;

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event VARCHAR(64) NOT NULL,
  correlation_id VARCHAR(64) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_audit_correlation (correlation_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
