-- Day225: dual-system-ms minimal schemas (users/orders/payments)
-- Charset / collation kept simple for demo.

CREATE DATABASE IF NOT EXISTS ds_user DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS ds_order DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS ds_payment DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_0900_ai_ci;

-- ----------------------------
-- user-service (ds_user)
-- ----------------------------
USE ds_user;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_users_username (username)
);

-- ----------------------------
-- order-service (ds_order)
-- ----------------------------
USE ds_order;

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  status VARCHAR(16) NOT NULL,
  paid_payment_id BIGINT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_orders_user_id_created_at (user_id, created_at),
  KEY idx_orders_status_created_at (status, created_at),
  UNIQUE KEY uk_orders_paid_payment_id (paid_payment_id)
);

-- optional
CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  sku VARCHAR(64) NOT NULL,
  qty INT NOT NULL,
  price DECIMAL(18,2) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_order_items_order_id (order_id)
);

CREATE TABLE IF NOT EXISTS processed_payment_events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id VARCHAR(64) NOT NULL,
  payment_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_processed_payment_event_id (event_id),
  KEY idx_processed_payment_order_id (order_id)
);

-- ----------------------------
-- payment-service (ds_payment)
-- ----------------------------
USE ds_payment;

CREATE TABLE IF NOT EXISTS payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  status VARCHAR(16) NOT NULL,
  paid_at TIMESTAMP(3) NULL,
  order_marked_paid_at TIMESTAMP(3) NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_payments_user_id_created_at (user_id, created_at),
  KEY idx_payments_status_created_at (status, created_at),
  UNIQUE KEY uk_payments_order_id (order_id)
);

-- W34 Day235: transactional outbox (payment SUCCESS + payload in one DB commit; MQ send async)
CREATE TABLE IF NOT EXISTS payment_outbox (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  payment_id BIGINT NOT NULL,
  payload_json TEXT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  retry_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_payment_outbox_payment_id (payment_id),
  KEY idx_payment_outbox_status_created (status, created_at)
);
