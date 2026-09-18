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