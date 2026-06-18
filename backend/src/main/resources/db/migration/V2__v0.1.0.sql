-- ==============================================================
-- Millete v0.1.0 - Sessions, Preferences, FSM, Savings Goals, Refactors & Renames
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

-- 14. PREMIUM FIELDS (ALTER USERS) + Telegram chatId
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_premium BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS license VARCHAR(100);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS premium_tier VARCHAR(20) NOT NULL DEFAULT 'FREE';

ALTER TABLE users
    ADD CONSTRAINT chk_premium_tier
        CHECK (premium_tier IN ('FREE', 'BASIC', 'PRO', 'ENTERPRISE'));

ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_chat_id BIGINT;
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_telegram_chat ON users(telegram_chat_id) WHERE telegram_chat_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_license ON users(license)
    WHERE license IS NOT NULL;

-- 15. LAST EXECUTED DATE (ALTER PLANNED TRANSACTIONS)
ALTER TABLE planned_transactions
    ADD COLUMN IF NOT EXISTS last_executed_date DATE;

-- 16. FAMILY INVITATIONS REFACTOR (INTERNAL INVITATIONS)
ALTER TABLE family_invitations ALTER COLUMN email DROP NOT NULL;
ALTER TABLE family_invitations ALTER COLUMN token DROP NOT NULL;

ALTER TABLE family_invitations
    ADD COLUMN IF NOT EXISTS inviter_user_id UUID,
    ADD COLUMN IF NOT EXISTS invited_user_id UUID;

ALTER TABLE family_invitations
DROP CONSTRAINT IF EXISTS chk_invitation_status,
    ADD CONSTRAINT chk_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED'));

ALTER TABLE family_invitations
    ADD CONSTRAINT fk_invitations_inviter FOREIGN KEY (inviter_user_id) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_invitations_invited FOREIGN KEY (invited_user_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_invitations_invited ON family_invitations(invited_user_id, status);
CREATE INDEX IF NOT EXISTS idx_invitations_family_status ON family_invitations(family_id, status);

-- ==============================================================
-- 17. REFACTOR family_* -> goal_* (fusionado de la antigua V3)
-- ==============================================================

-- RENAME TABLES
ALTER TABLE family_units RENAME TO goal_units;
ALTER TABLE family_members RENAME TO goal_members;
ALTER TABLE family_invitations RENAME TO goal_invitations;
ALTER TABLE family_contributions RENAME TO goal_contributions;

-- RENAME COLUMNS IN goal_members
ALTER TABLE goal_members RENAME COLUMN family_id TO goal_id;

-- RENAME COLUMNS IN goal_invitations
ALTER TABLE goal_invitations RENAME COLUMN family_id TO goal_id;

-- RENAME COLUMNS IN goal_contributions
ALTER TABLE goal_contributions RENAME COLUMN family_id TO goal_id;

-- DROP OLD CONSTRAINTS ON goal_members
ALTER TABLE goal_members DROP CONSTRAINT IF EXISTS uq_family_user;
ALTER TABLE goal_members DROP CONSTRAINT IF EXISTS fk_members_family;
ALTER TABLE goal_members DROP CONSTRAINT IF EXISTS fk_members_user;

-- ADD NEW CONSTRAINTS ON goal_members
ALTER TABLE goal_members
    ADD CONSTRAINT uq_goal_user UNIQUE (goal_id, user_id),
    ADD CONSTRAINT fk_members_goal FOREIGN KEY (goal_id) REFERENCES goal_units(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- DROP OLD CONSTRAINTS ON goal_invitations
ALTER TABLE goal_invitations DROP CONSTRAINT IF EXISTS chk_invitation_status;
ALTER TABLE goal_invitations DROP CONSTRAINT IF EXISTS fk_invitations_family;
ALTER TABLE goal_invitations DROP CONSTRAINT IF EXISTS fk_invitations_inviter;
ALTER TABLE goal_invitations DROP CONSTRAINT IF EXISTS fk_invitations_invited;

-- ADD NEW CONSTRAINTS ON goal_invitations
ALTER TABLE goal_invitations
    ADD CONSTRAINT chk_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED')),
    ADD CONSTRAINT fk_invitations_goal FOREIGN KEY (goal_id) REFERENCES goal_units(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_invitations_inviter FOREIGN KEY (inviter_user_id) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_invitations_invited FOREIGN KEY (invited_user_id) REFERENCES users(id) ON DELETE SET NULL;

-- DROP OLD CONSTRAINTS ON goal_contributions
ALTER TABLE goal_contributions DROP CONSTRAINT IF EXISTS fk_contributions_family;
ALTER TABLE goal_contributions DROP CONSTRAINT IF EXISTS fk_contributions_user;

-- ADD NEW CONSTRAINTS ON goal_contributions
ALTER TABLE goal_contributions
    ADD CONSTRAINT fk_contributions_goal FOREIGN KEY (goal_id) REFERENCES goal_units(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_contributions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- DROP OLD INDEXES
DROP INDEX IF EXISTS idx_family_members_family;
DROP INDEX IF EXISTS idx_family_members_user;
DROP INDEX IF EXISTS idx_family_invitations_token;
DROP INDEX IF EXISTS idx_family_invitations_family;
DROP INDEX IF EXISTS idx_invitations_invited;
DROP INDEX IF EXISTS idx_invitations_family_status;
DROP INDEX IF EXISTS idx_family_contributions_family;

-- CREATE NEW INDEXES
CREATE INDEX IF NOT EXISTS idx_goal_members_goal ON goal_members(goal_id);
CREATE INDEX IF NOT EXISTS idx_goal_members_user ON goal_members(user_id);
CREATE INDEX IF NOT EXISTS idx_goal_invitations_token ON goal_invitations(token);
CREATE INDEX IF NOT EXISTS idx_goal_invitations_goal ON goal_invitations(goal_id);
CREATE INDEX IF NOT EXISTS idx_goal_invitations_invited ON goal_invitations(invited_user_id, status);
CREATE INDEX IF NOT EXISTS idx_goal_invitations_goal_status ON goal_invitations(goal_id, status);
CREATE INDEX IF NOT EXISTS idx_goal_contributions_goal ON goal_contributions(goal_id);

-- REMOVE OBSOLETE FOREIGN KEY FROM CATEGORIES
ALTER TABLE categories DROP CONSTRAINT IF EXISTS fk_categories_family;