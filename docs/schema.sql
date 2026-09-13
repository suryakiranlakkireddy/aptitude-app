-- Aptitude App database schema (PostgreSQL)
-- This mirrors the JPA entities in backend/src/main/java/com/aptitudeapp/entity
-- Spring Boot (ddl-auto: update) will create/update these automatically,
-- but this file documents the intended structure and can be used to provision manually.

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER', -- USER | ADMIN
    profile_image_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS topics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    category VARCHAR(20) DEFAULT 'BOTH' -- GOVT | IT | BOTH
);

CREATE TABLE IF NOT EXISTS quizzes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    topic_id BIGINT NOT NULL REFERENCES topics(id),
    duration_minutes INT DEFAULT 20,
    mock_test BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    explanation TEXT
);

CREATE TABLE IF NOT EXISTS question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    correct BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id),
    total_questions INT,
    correct_count INT,
    score_percent DOUBLE PRECISION,
    time_taken_seconds INT,
    attempted_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS attempt_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id),
    selected_option_id BIGINT REFERENCES question_options(id),
    correct BOOLEAN DEFAULT FALSE
);

-- Per-student, isolated progress: unique per (user_id, topic_id) so every
-- student's stats are tracked separately and never mixed with another user's.
CREATE TABLE IF NOT EXISTS progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    topic_id BIGINT NOT NULL REFERENCES topics(id),
    total_attempts INT DEFAULT 0,
    total_correct INT DEFAULT 0,
    total_wrong INT DEFAULT 0,
    completion_percent DOUBLE PRECISION DEFAULT 0,
    current_streak_days INT DEFAULT 0,
    last_activity_at TIMESTAMP,
    UNIQUE (user_id, topic_id)
);

-- Subscription: Rs 59, valid 4 months, must be repurchased (repayment) after expiry.
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    amount_paid DOUBLE PRECISION,
    purchased_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' -- ACTIVE | EXPIRED
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    subscription_id BIGINT REFERENCES subscriptions(id),
    amount DOUBLE PRECISION,
    transaction_id VARCHAR(255),
    payment_method VARCHAR(50),
    status VARCHAR(20), -- SUCCESS | FAILED | PENDING
    paid_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS notes (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL REFERENCES topics(id),
    title VARCHAR(255),
    file_url TEXT NOT NULL,      -- Supabase Storage OBJECT PATH (not a public URL) - signed on read
    thumbnail_url TEXT,          -- Supabase Storage OBJECT PATH (optional) - signed on read
    uploaded_at TIMESTAMP NOT NULL DEFAULT now()
);

-- FCM registration tokens for push notifications. A token can get reassigned to a
-- different user (logout + different login on the same device) - see
-- NotificationController#register, which repoints rather than duplicating.
CREATE TABLE IF NOT EXISTS device_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token TEXT NOT NULL UNIQUE,
    platform VARCHAR(20),        -- ANDROID | IOS | WEB
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
