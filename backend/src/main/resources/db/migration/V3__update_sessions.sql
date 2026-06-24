-- ==============================================================
-- Millete v0.2.0 - Profile Management, Multi-Session Support
-- ==============================================================

-- 1. Eliminar la restricción que impide múltiples sesiones en el mismo canal
ALTER TABLE user_sessions DROP CONSTRAINT IF EXISTS uq_user_channel;

-- 2. Añadir control de estado para poder cerrar sesiones remotamente
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

-- 3. Actualizar índice para incluir estado activo
CREATE INDEX IF NOT EXISTS idx_sessions_user_active ON user_sessions(user_id, active) WHERE active = TRUE;
