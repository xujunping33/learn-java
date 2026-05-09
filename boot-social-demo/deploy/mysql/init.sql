-- boot-social-demo：Compose 首次初始化（docker-entrypoint-initdb.d）
-- 须与 compose / .env 中 MYSQL_DATABASE、DB_URL 里库名一致（默认 ssm_social）。
-- Day192：收口到本目录；修改库名时请同步本文与 `MYSQL_DATABASE`。

CREATE DATABASE IF NOT EXISTS ssm_social
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE ssm_social;

-- users: accounts
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  salt VARCHAR(64) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- posts: authored content
CREATE TABLE IF NOT EXISTS posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  cover_object_key VARCHAR(255) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_posts_user_id FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_posts_user_created (user_id, created_at),
  INDEX idx_posts_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- comments: post replies
CREATE TABLE IF NOT EXISTS comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comments_post_id FOREIGN KEY (post_id) REFERENCES posts(id),
  CONSTRAINT fk_comments_user_id FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_comments_post_created (post_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- post_likes: one user can like one post once
CREATE TABLE IF NOT EXISTS post_likes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_post_likes_post_id FOREIGN KEY (post_id) REFERENCES posts(id),
  CONSTRAINT fk_post_likes_user_id FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT uk_post_likes_post_user UNIQUE (post_id, user_id),
  INDEX idx_post_likes_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- notifications: async notifications (W29 Day201)
CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(64) NOT NULL,
  ref_id BIGINT NOT NULL,
  payload TEXT NULL,
  dedup_key VARCHAR(128) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_notifications_dedup UNIQUE (dedup_key),
  INDEX idx_notifications_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- outbox: reliable event publish (W30 Day206)
CREATE TABLE IF NOT EXISTS outbox_events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  exchange_name VARCHAR(128) NOT NULL,
  routing_key VARCHAR(128) NOT NULL,
  payload_type VARCHAR(255) NOT NULL,
  payload_json TEXT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_error VARCHAR(512) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sent_at DATETIME NULL,
  INDEX idx_outbox_status_retry (status, next_retry_at),
  INDEX idx_outbox_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
