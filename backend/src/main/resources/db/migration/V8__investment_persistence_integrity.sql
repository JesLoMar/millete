-- Strengthen provenance links for derived FIFO lots and their sell allocations.
ALTER TABLE activities
    ADD CONSTRAINT uq_activities_id_user_type UNIQUE (id, user_id, type),
    ADD CONSTRAINT uq_activities_id_user_asset_type UNIQUE (id, user_id, asset_id, type);

ALTER TABLE holdings
    ADD CONSTRAINT uq_holdings_id_user UNIQUE (id, user_id),
    ADD CONSTRAINT uq_holdings_id_user_asset UNIQUE (id, user_id, asset_id);

ALTER TABLE lots
    ADD CONSTRAINT uq_lots_id_user UNIQUE (id, user_id),
    ADD COLUMN source_activity_type VARCHAR(24) NOT NULL DEFAULT 'BUY',
    ADD CONSTRAINT chk_lot_source_activity_type CHECK (source_activity_type = 'BUY'),
    ADD CONSTRAINT fk_lot_source_buy FOREIGN KEY
        (source_activity_id, user_id, asset_id, source_activity_type)
        REFERENCES activities (id, user_id, asset_id, type) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_lot_source_holding_user_asset FOREIGN KEY
        (source_holding_id, user_id, asset_id)
        REFERENCES holdings (id, user_id, asset_id) ON DELETE RESTRICT;

ALTER TABLE lot_consumptions
    ADD COLUMN sell_activity_type VARCHAR(24) NOT NULL DEFAULT 'SELL',
    ADD CONSTRAINT chk_consumption_sell_activity_type CHECK (sell_activity_type = 'SELL'),
    ADD CONSTRAINT fk_consumption_sell_activity_type FOREIGN KEY
        (sell_activity_id, user_id, sell_activity_type)
        REFERENCES activities (id, user_id, type) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_consumption_lot_user FOREIGN KEY (lot_id, user_id)
        REFERENCES lots (id, user_id) ON DELETE RESTRICT;

CREATE INDEX idx_lot_consumptions_sell
    ON lot_consumptions (user_id, sell_activity_id, lot_id);

-- A row CHECK cannot cap the sum allocated to a lot. Locking the lot row
-- serializes concurrent allocations before the aggregate is checked.
CREATE FUNCTION enforce_lot_consumption_total() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    lot_original_quantity DECIMAL(28,12);
    lot_asset_id UUID;
    sell_asset_id UUID;
    already_consumed DECIMAL(28,12);
BEGIN
    IF TG_OP = 'UPDATE' AND (
        NEW.lot_id IS DISTINCT FROM OLD.lot_id OR
        NEW.user_id IS DISTINCT FROM OLD.user_id OR
        NEW.sell_activity_id IS DISTINCT FROM OLD.sell_activity_id
    ) THEN
        RAISE EXCEPTION 'FIFO consumption source links are immutable; replace the derived allocation set instead'
            USING ERRCODE = '23514';
    END IF;

    SELECT original_quantity, asset_id
      INTO lot_original_quantity, lot_asset_id
      FROM lots
     WHERE id = NEW.lot_id AND user_id = NEW.user_id
     FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'FIFO lot % does not belong to user %', NEW.lot_id, NEW.user_id
            USING ERRCODE = '23503';
    END IF;

    SELECT asset_id
      INTO sell_asset_id
      FROM activities
     WHERE id = NEW.sell_activity_id
       AND user_id = NEW.user_id
       AND type = 'SELL';
    IF NOT FOUND THEN
        RAISE EXCEPTION 'FIFO consumption must reference a SELL activity owned by the same user'
            USING ERRCODE = '23503';
    END IF;
    IF sell_asset_id IS DISTINCT FROM lot_asset_id THEN
        RAISE EXCEPTION 'FIFO consumption lot and SELL activity must reference the same asset'
            USING ERRCODE = '23514';
    END IF;

    SELECT COALESCE(SUM(quantity), 0)
      INTO already_consumed
      FROM lot_consumptions
     WHERE lot_id = NEW.lot_id
       AND user_id = NEW.user_id
       AND (TG_OP <> 'UPDATE' OR id <> NEW.id);

    IF already_consumed + NEW.quantity > lot_original_quantity THEN
        RAISE EXCEPTION 'FIFO consumptions exceed original lot quantity for lot %', NEW.lot_id
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_enforce_lot_consumption_total
    BEFORE INSERT OR UPDATE ON lot_consumptions
    FOR EACH ROW EXECUTE FUNCTION enforce_lot_consumption_total();

-- Prevent overlapping local-currency periods, including concurrent first-use
-- backfills and preference changes for the same account.
CREATE FUNCTION enforce_user_currency_period_nonoverlap() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM pg_advisory_xact_lock(hashtextextended('user-local-currency:' || NEW.user_id::text, 0));

    IF EXISTS (
        SELECT 1
          FROM user_local_currency_history existing
         WHERE existing.user_id = NEW.user_id
           AND existing.id <> NEW.id
           AND tstzrange(existing.valid_from, existing.valid_to, '[)')
               && tstzrange(NEW.valid_from, NEW.valid_to, '[)')
    ) THEN
        RAISE EXCEPTION 'Local currency periods must not overlap for user %', NEW.user_id
            USING ERRCODE = '23P01';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_enforce_user_currency_period_nonoverlap
    BEFORE INSERT OR UPDATE ON user_local_currency_history
    FOR EACH ROW EXECUTE FUNCTION enforce_user_currency_period_nonoverlap();
