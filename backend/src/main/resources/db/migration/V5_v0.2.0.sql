-- Bug 3: Alinear precisión de budget_limit con el resto de campos monetarios
ALTER TABLE categories
    ALTER COLUMN budget_limit TYPE numeric(12, 2);

-- Bug 2: Índice único parcial para evitar nombres duplicados por usuario
-- Solo aplica a categorías activas (soft delete no bloquea recrear)
-- Se usa LOWER(name) para que la unicidad sea case-insensitive
CREATE UNIQUE INDEX idx_categories_user_name_active
    ON categories (user_id, LOWER(name))
    WHERE active = true;