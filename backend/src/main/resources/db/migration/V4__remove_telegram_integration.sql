-- ============================================================================
-- V4: Remove Telegram integration after the bot is decommissioned.
-- ----------------------------------------------------------------------------
-- Deletes all Telegram sessions and drops Telegram-related columns, indexes,
-- and tables that are no longer used by the backend.
-- ============================================================================

-- 1. Remove persisted Telegram bot state and sessions.
DROP TABLE IF EXISTS telegram_fsm_context;
DELETE FROM user_sessions WHERE channel = 'TELEGRAM';

-- 2. Remove Telegram columns and indexes from users.
DROP INDEX IF EXISTS idx_users_telegram_chat;
ALTER TABLE users DROP COLUMN IF EXISTS telegram_chat_id;

-- 3. Remove Telegram columns and indexes from user_sessions.
DROP INDEX IF EXISTS uq_user_telegram_session;
ALTER TABLE user_sessions DROP COLUMN IF EXISTS telegram_chat_id;

-- 4. Restrict channel values to the only supported channel.
ALTER TABLE user_sessions DROP CONSTRAINT IF EXISTS chk_session_channel;
ALTER TABLE user_sessions ADD CONSTRAINT chk_session_channel
    CHECK (channel IN ('WEB'));
