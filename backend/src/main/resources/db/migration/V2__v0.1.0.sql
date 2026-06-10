-- ==============================================================
-- Millete v0.1.0 - Sessions, Preferences, FSM, Savings Goals & +
-- ==============================================================

-- 10. USER PREFERENCES
CREATE TABLE user_preferences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    preferences JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_preferences_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_preferences_user ON user_preferences(user_id);

-- 11. USER SESSIONS
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    channel VARCHAR(20) NOT NULL,
    telegram_chat_id BIGINT,
    login_attempts INT NOT NULL DEFAULT 0,
    blocked_until TIMESTAMP,
    last_attempt_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_channel UNIQUE (user_id, channel),
    CONSTRAINT chk_session_channel CHECK (channel IN ('WEB', 'TELEGRAM')),
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_sessions_user_channel ON user_sessions(user_id, channel);
CREATE INDEX idx_sessions_blocked ON user_sessions(blocked_until) 
    WHERE blocked_until IS NOT NULL;

-- 12. TELEGRAM FSM CONTEXT
CREATE TABLE telegram_fsm_context (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    current_state VARCHAR(50) NOT NULL,
    context_data JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_fsm_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_fsm_user ON telegram_fsm_context(user_id);
CREATE INDEX idx_fsm_state ON telegram_fsm_context(current_state);

-- 13. SAVINGS GOALS
CREATE TABLE savings_goals (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    target_amount DECIMAL(12, 2) NOT NULL,
    current_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    deadline DATE,
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    link VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_target_positive CHECK (target_amount > 0),
    CONSTRAINT chk_current_not_negative CHECK (current_amount >= 0),
    CONSTRAINT chk_goal_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_goal_status CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_goals_user ON savings_goals(user_id);
CREATE INDEX idx_goals_status ON savings_goals(user_id, status);

-- 14. PREMIUM FIELDS (ALTER USERS)
ALTER TABLE users 
ADD COLUMN is_premium BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users 
ADD COLUMN license VARCHAR(100);

ALTER TABLE users 
ADD COLUMN premium_tier VARCHAR(20) NOT NULL DEFAULT 'FREE';

ALTER TABLE users 
ADD CONSTRAINT chk_premium_tier 
CHECK (premium_tier IN ('FREE', 'BASIC', 'PRO', 'ENTERPRISE'));

CREATE INDEX idx_users_license ON users(license) 
WHERE license IS NOT NULL;

-- 15. LAST EXECUTED DATE (ALTER PLANNED TRANSACTIONS)
ALTER TABLE planned_transactions 
ADD COLUMN last_executed_date DATE;