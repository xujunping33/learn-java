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
