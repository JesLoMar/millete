-- Bug 3: Alinear precisión de budget_limit con el resto de campos monetarios
ALTER TABLE categories
    ALTER COLUMN budget_limit TYPE numeric(12, 2);

-- Bug 2: Índice único parcial para evitar nombres duplicados por usuario
-- Solo aplica a categorías activas (soft delete no bloquea recrear)
-- Se usa LOWER(name) para que la unicidad sea case-insensitive
CREATE UNIQUE INDEX idx_categories_user_name_active
    ON categories (user_id, LOWER(name))
    WHERE active = true;
-- V5: Añadir contador de fallos consecutivos a planned_transactions
-- para desactivación automática tras 3 fallos.

ALTER TABLE planned_transactions
    ADD COLUMN IF NOT EXISTS failure_count INTEGER NOT NULL DEFAULT 0;

COMMENT ON COLUMN planned_transactions.failure_count IS
    'Número de fallos consecutivos en la ejecución del scheduler. Al llegar a 3, la plantilla se desactiva automáticamente.';

-- ============================================================================
-- groupgoals: limpieza, contribuciones con tipo y NOT NULL en monthly_target
-- ============================================================================

-- 1. monthly_target obligatorio en goal_units
UPDATE goal_units
SET monthly_target = 0
WHERE monthly_target IS NULL;

ALTER TABLE goal_units
    ALTER COLUMN monthly_target SET NOT NULL;

-- 2. Eliminar columna token de goal_invitations (flujo no implementado)
ALTER TABLE goal_invitations
    DROP COLUMN IF EXISTS token;

DROP INDEX IF EXISTS idx_goal_invitations_token;

-- 3. Añadir tipo de contribución (DEPOSIT / WITHDRAWAL)
ALTER TABLE goal_contributions
    ADD COLUMN IF NOT EXISTS type VARCHAR(20) NOT NULL DEFAULT 'DEPOSIT';

ALTER TABLE goal_contributions
    ADD CONSTRAINT chk_contribution_type
        CHECK (type IN ('DEPOSIT', 'WITHDRAWAL'));