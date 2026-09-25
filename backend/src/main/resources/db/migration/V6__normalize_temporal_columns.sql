-- ============================================================================
-- V6: Normalización de columnas temporales
-- ============================================================================
--
-- Históricamente las columnas TIMESTAMP almacenaban valores correspondientes
-- a la hora local de Europe/Madrid.
--
-- Al convertirlas a TIMESTAMPTZ se interpreta el valor histórico como hora
-- de Europe/Madrid, conservando así el instante real representado.
--
-- Las fechas de dominio:
--   - transactions.date
--   - investments.purchase_date
--
-- representan días de calendario y se convierten a DATE.
--
-- transactions.date contiene actualmente algunos valores con hora distinta
-- de medianoche. Se acepta explícitamente esa pérdida de hora porque esa hora
-- corresponde al momento en que se registraron datos de prueba y no forma
-- parte del significado de la transacción.
-- ============================================================================


-- ============================================================================
-- 1. TRANSACTIONS
-- ============================================================================

ALTER TABLE transactions
    ALTER COLUMN date TYPE DATE
        USING date::date,
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 2. PLANNED TRANSACTIONS
-- ============================================================================

ALTER TABLE planned_transactions
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 3. NOTIFICATIONS
-- ============================================================================

ALTER TABLE notifications
    ALTER COLUMN actioned_at TYPE TIMESTAMPTZ
        USING actioned_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN expires_at TYPE TIMESTAMPTZ
        USING expires_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 4. USER SESSIONS
-- ============================================================================

ALTER TABLE user_sessions
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 5. USER PREFERENCES
-- ============================================================================

ALTER TABLE user_preferences
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 6. USERS
-- ============================================================================

ALTER TABLE users
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 7. CATEGORIES
-- ============================================================================

ALTER TABLE categories
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 8. INVESTMENTS
-- ============================================================================
--
-- purchase_date es una fecha de calendario, por lo que se convierte a DATE.
-- ============================================================================

ALTER TABLE investments
    ALTER COLUMN purchase_date TYPE DATE
        USING purchase_date::date,
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 9. SAVINGS GOALS
-- ============================================================================

ALTER TABLE savings_goals
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 10. GOAL CONTRIBUTIONS
-- ============================================================================
--
-- date era TIMESTAMP con DEFAULT CURRENT_TIMESTAMP.
-- Se elimina temporalmente el DEFAULT para evitar problemas al cambiar
-- el tipo y se vuelve a establecer después como CURRENT_TIMESTAMP.
-- ============================================================================

ALTER TABLE goal_contributions
    ALTER COLUMN date DROP DEFAULT,
    ALTER COLUMN date TYPE TIMESTAMPTZ
        USING date AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN date SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 11. GOAL INVITATIONS
-- ============================================================================

ALTER TABLE goal_invitations
    ALTER COLUMN expires_at TYPE TIMESTAMPTZ
        USING expires_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 12. GOAL MEMBERS
-- ============================================================================

ALTER TABLE goal_members
    ALTER COLUMN joined_at TYPE TIMESTAMPTZ
        USING joined_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';


-- ============================================================================
-- 13. GOAL UNITS
-- ============================================================================

ALTER TABLE goal_units
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';