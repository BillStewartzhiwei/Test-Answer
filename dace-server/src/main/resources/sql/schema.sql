CREATE DATABASE IF NOT EXISTS dace DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dace;

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  openid VARCHAR(64) NOT NULL UNIQUE,
  nickname VARCHAR(50) NOT NULL,
  avatar_url VARCHAR(255),
  role ENUM('creator','participant') NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE spaces (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  member_count INT NOT NULL DEFAULT 0,
  quiz_count INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_spaces_owner (owner_id),
  CONSTRAINT fk_spaces_owner FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE invite_codes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  space_id BIGINT NOT NULL,
  code VARCHAR(10) NOT NULL UNIQUE,
  max_uses INT NOT NULL DEFAULT 50,
  used_count INT NOT NULL DEFAULT 0,
  expire_at DATETIME NOT NULL,
  is_active TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_invite_codes_space (space_id),
  CONSTRAINT fk_invite_codes_space FOREIGN KEY (space_id) REFERENCES spaces(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE space_members (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  space_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  invite_code_id BIGINT,
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_space_members_space_user (space_id, user_id),
  INDEX idx_space_members_user (user_id),
  CONSTRAINT fk_space_members_space FOREIGN KEY (space_id) REFERENCES spaces(id),
  CONSTRAINT fk_space_members_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_space_members_invite FOREIGN KEY (invite_code_id) REFERENCES invite_codes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quizzes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  space_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  time_limit INT NOT NULL DEFAULT 0,
  total_score INT NOT NULL DEFAULT 0,
  question_count INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  publish_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_quizzes_space_status (space_id, status),
  CONSTRAINT fk_quizzes_space FOREIGN KEY (space_id) REFERENCES spaces(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE questions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  quiz_id BIGINT NOT NULL,
  sort_order INT NOT NULL,
  type ENUM('single','multiple','judge') NOT NULL,
  content TEXT NOT NULL,
  score INT NOT NULL DEFAULT 5,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_questions_quiz_order (quiz_id, sort_order),
  CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE options (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  label CHAR(1) NOT NULL,
  content VARCHAR(500) NOT NULL,
  is_correct TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_options_question_label (question_id, label),
  CONSTRAINT fk_options_question FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz_attempts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  quiz_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  space_id BIGINT NOT NULL,
  score INT NOT NULL DEFAULT 0,
  total_score INT NOT NULL DEFAULT 0,
  start_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  submit_at DATETIME,
  duration_sec INT,
  status TINYINT NOT NULL DEFAULT 1,
  UNIQUE KEY uk_quiz_attempts_quiz_user (quiz_id, user_id),
  INDEX idx_quiz_attempts_space (space_id),
  CONSTRAINT fk_quiz_attempts_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
  CONSTRAINT fk_quiz_attempts_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_quiz_attempts_space FOREIGN KEY (space_id) REFERENCES spaces(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE answers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  attempt_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  selected_option VARCHAR(10) NOT NULL,
  is_correct TINYINT NOT NULL DEFAULT 0,
  score_earned INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_answers_attempt_question (attempt_id, question_id),
  CONSTRAINT fk_answers_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id),
  CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
