-- ============================================================================
-- V6: Normalización temporal (Fases 1 + 2 del plan de temporalidad)
-- ----------------------------------------------------------------------------
-- Fusiona en una sola migración los cambios temporales ya desarrollados:
--   * Fase 1: clave "timezone" en user_preferences + auditoría TIMESTAMPTZ.
--   * Fase 2: conversión TIMESTAMP -> TIMESTAMPTZ en groupgoals y notifications.
--
-- Regla de conversión: todos los valores TIMESTAMP existentes fueron escritos
-- por la aplicación como hora local del servidor (LocalDateTime.now()) sobre
-- un PostgreSQL cuya session_timezone se asume UTC (configuración estándar del
-- Dockerfile). Se declara explícitamente AT TIME ZONE 'UTC' para que la
-- reinterpretación sea determinista e independiente de la zona de sesión del
-- cliente que ejecute la migración, evitando desplazamientos sorpresa.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. user_preferences: zona horaria por usuario (default técnico: UTC)
-- ----------------------------------------------------------------------------
-- Los usuarios sin fila de preferencias reciben UTC implícitamente en la capa
-- de aplicación, por lo que no es necesario insertar filas nuevas.
UPDATE user_preferences
SET preferences = preferences || '{"timezone": "UTC"}'::jsonb
WHERE NOT (preferences ? 'timezone');

COMMENT ON COLUMN user_preferences.preferences IS
    'Preferencias de usuario en JSONB. Clave "timezone": identificador de zona IANA (por ejemplo Europe/Madrid). Default técnico: UTC.';

-- ----------------------------------------------------------------------------
-- 2. user_preferences: auditoría temporal con zona
-- ----------------------------------------------------------------------------
ALTER TABLE user_preferences
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'UTC';

-- ----------------------------------------------------------------------------
-- 3. groupgoals: timestamps con zona
-- ----------------------------------------------------------------------------
ALTER TABLE goal_invitations
    ALTER COLUMN expires_at  TYPE TIMESTAMPTZ USING expires_at  AT TIME ZONE 'UTC',
    ALTER COLUMN created_at  TYPE TIMESTAMPTZ USING created_at  AT TIME ZONE 'UTC',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ USING modified_at AT TIME ZONE 'UTC';

ALTER TABLE goal_contributions
    ALTER COLUMN date        TYPE TIMESTAMPTZ USING date        AT TIME ZONE 'UTC',
    ALTER COLUMN created_at  TYPE TIMESTAMPTZ USING created_at  AT TIME ZONE 'UTC',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ USING modified_at AT TIME ZONE 'UTC';

ALTER TABLE goal_members
    ALTER COLUMN joined_at   TYPE TIMESTAMPTZ USING joined_at   AT TIME ZONE 'UTC',
    ALTER COLUMN created_at  TYPE TIMESTAMPTZ USING created_at  AT TIME ZONE 'UTC',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ USING modified_at AT TIME ZONE 'UTC';

ALTER TABLE goal_units
    ALTER COLUMN created_at  TYPE TIMESTAMPTZ USING created_at  AT TIME ZONE 'UTC',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ USING modified_at AT TIME ZONE 'UTC';

-- ----------------------------------------------------------------------------
-- 4. notifications: timestamps con zona
-- ----------------------------------------------------------------------------
ALTER TABLE notifications
    ALTER COLUMN actioned_at TYPE TIMESTAMPTZ USING actioned_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at  TYPE TIMESTAMPTZ USING created_at  AT TIME ZONE 'UTC',
    ALTER COLUMN expires_at  TYPE TIMESTAMPTZ USING expires_at  AT TIME ZONE 'UTC';
