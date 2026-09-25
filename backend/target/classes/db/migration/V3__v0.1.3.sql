-- ============================================================================
-- V3: Separación de sesiones y seguridad de login (refactor arquitectónico)
-- ----------------------------------------------------------------------------
-- ============================================================================
-- 1. NUEVA TABLA: user_login_security (estado de seguridad por cuenta)
-- ============================================================================
CREATE TABLE user_login_security (
                                     user_id          UUID PRIMARY KEY,
                                     failed_attempts  INTEGER NOT NULL DEFAULT 0,
                                     blocked_until    TIMESTAMPTZ,
                                     last_attempt_at  TIMESTAMPTZ,
                                     created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     modified_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                     CONSTRAINT fk_login_security_user
                                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE user_login_security IS
    'Estado de seguridad de login por cuenta: intentos fallidos y bloqueo temporal. Una fila por usuario. Los intentos NO viven en user_sessions.';
COMMENT ON COLUMN user_login_security.blocked_until IS
    'Momento hasta el que la cuenta está bloqueada. NULL = no bloqueada. El desbloqueo es lazy (se evalúa en cada intento).';
CREATE INDEX idx_login_security_blocked
    ON user_login_security(blocked_until)
    WHERE blocked_until IS NOT NULL;


-- ============================================================================
-- 2. MIGRACIÓN DE DATOS: user_sessions -> user_login_security
-- ----------------------------------------------------------------------------
INSERT INTO user_login_security (
    user_id, failed_attempts, blocked_until, last_attempt_at, created_at, modified_at
)
SELECT
    user_id,
    MAX(login_attempts),
    MAX(blocked_until),
    MAX(last_attempt_at),
    MIN(created_at),
    MAX(modified_at)
FROM user_sessions
GROUP BY user_id;


-- ============================================================================
-- 3. LIMPIEZA DE user_sessions: queda SOLO como tabla de sesiones
-- ============================================================================

DROP INDEX IF EXISTS idx_sessions_blocked;
ALTER TABLE user_sessions DROP COLUMN login_attempts;
ALTER TABLE user_sessions DROP COLUMN blocked_until;
ALTER TABLE user_sessions DROP COLUMN last_attempt_at;


-- ============================================================================
-- 4. REGLA DE NEGOCIO EN BBDD: una sola sesión TELEGRAM por usuario
-- ----------------------------------------------------------------------------
CREATE UNIQUE INDEX uq_user_telegram_session
    ON user_sessions(user_id)
    WHERE channel = 'TELEGRAM';