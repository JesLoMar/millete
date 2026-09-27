ALTER TABLE lots
    ADD COLUMN acquisition_order BIGINT;

WITH ranked_lots AS (
    SELECT l.id,
           ROW_NUMBER() OVER (
               PARTITION BY l.user_id, l.asset_id, l.acquired_at
               ORDER BY COALESCE(a.ordering_key, 0), COALESCE(a.id, h.id)::text COLLATE "C"
           ) - 1 AS acquisition_order
    FROM lots l
    LEFT JOIN activities a ON a.id = l.source_activity_id
    LEFT JOIN holdings h ON h.id = l.source_holding_id
)
UPDATE lots l
SET acquisition_order = ranked_lots.acquisition_order
FROM ranked_lots
WHERE l.id = ranked_lots.id;

ALTER TABLE lots
    ALTER COLUMN acquisition_order SET NOT NULL;

DROP INDEX idx_lots_open_fifo;

CREATE INDEX idx_lots_open_fifo
    ON lots(user_id, asset_id, acquired_at, acquisition_order, id)
    WHERE remaining_quantity > 0;
