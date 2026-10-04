-- ============================================================================
-- 1. CATEGORIES
-- ============================================================================

-- Añadir descripción opcional de la categoría.
ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS description VARCHAR(500);

-- Bug 3: Alinear precisión de budget_limit con el resto de campos monetarios.
ALTER TABLE categories
    ALTER COLUMN budget_limit TYPE numeric(12, 2);

-- Bug 2: Índice único parcial para evitar nombres duplicados por usuario.
-- Solo aplica a categorías activas (soft delete no bloquea recrear).
-- Se usa LOWER(name) para que la unicidad sea case-insensitive.
CREATE UNIQUE INDEX idx_categories_user_name_active
    ON categories (user_id, LOWER(name))
    WHERE active = true;
-- ============================================================================
-- 2. PLANNED TRANSACTIONS
-- ============================================================================

-- Añadir contador de fallos consecutivos para desactivación automática tras
-- tres fallos de ejecución del scheduler.
ALTER TABLE planned_transactions
    ADD COLUMN IF NOT EXISTS failure_count INTEGER NOT NULL DEFAULT 0;

COMMENT ON COLUMN planned_transactions.failure_count IS
    'Número de fallos consecutivos en la ejecución del scheduler. Al llegar a 3, la plantilla se desactiva automáticamente.';

-- ============================================================================
-- 3. GOAL UNITS / GOAL CONTRIBUTIONS
-- ============================================================================

-- monthly_target es obligatorio.
UPDATE goal_units
SET monthly_target = 0
WHERE monthly_target IS NULL;

ALTER TABLE goal_units
    ALTER COLUMN monthly_target SET NOT NULL;

-- El flujo de invitaciones por token no está implementado.
ALTER TABLE goal_invitations
    DROP COLUMN IF EXISTS token;

DROP INDEX IF EXISTS idx_goal_invitations_token;

-- Añadir tipo de contribución (DEPOSIT / WITHDRAWAL).
ALTER TABLE goal_contributions
    ADD COLUMN IF NOT EXISTS type VARCHAR(20) NOT NULL DEFAULT 'DEPOSIT';

ALTER TABLE goal_contributions
    ADD CONSTRAINT chk_contribution_type
        CHECK (type IN ('DEPOSIT', 'WITHDRAWAL'));

-- ============================================================================
-- 4. NORMALIZE TEMPORAL COLUMNS
-- ============================================================================
--
-- Históricamente las columnas TIMESTAMP almacenaban valores correspondientes
-- a la hora local de Europe/Madrid. Al convertirlas a TIMESTAMPTZ se interpreta
-- el valor histórico como hora de Europe/Madrid, conservando el instante real.
--
-- Las fechas de dominio:
--   - transactions.date
-- representan días de calendario y se convierten a DATE.
--
-- La antigua investments.purchase_date también era DATE-compatible, pero la
-- tabla legacy investments se elimina en la sección de reemplazo, por lo que
-- no hace falta convertirla antes de eliminarla.
-- ============================================================================

ALTER TABLE transactions
    ALTER COLUMN date TYPE DATE
        USING date::date,
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE planned_transactions
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE notifications
    ALTER COLUMN actioned_at TYPE TIMESTAMPTZ
        USING actioned_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN expires_at TYPE TIMESTAMPTZ
        USING expires_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE user_sessions
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE user_preferences
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE users
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE categories
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE savings_goals
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE goal_contributions
    ALTER COLUMN date DROP DEFAULT,
    ALTER COLUMN date TYPE TIMESTAMPTZ
        USING date AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN date SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE goal_invitations
    ALTER COLUMN expires_at TYPE TIMESTAMPTZ
        USING expires_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE goal_members
    ALTER COLUMN joined_at TYPE TIMESTAMPTZ
        USING joined_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

ALTER TABLE goal_units
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'Europe/Madrid',
    ALTER COLUMN modified_at TYPE TIMESTAMPTZ
        USING modified_at AT TIME ZONE 'Europe/Madrid';

-- ============================================================================
-- 5. REPLACE LEGACY INVESTMENTS TABLE
-- ============================================================================

DROP TABLE IF EXISTS investments CASCADE;

-- ============================================================================
-- 6. ASSET SECTORS
-- ============================================================================

CREATE TABLE asset_sectors (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO asset_sectors(id, code, display_name) VALUES
('00000000-0000-0000-0000-000000000001', 'TECHNOLOGY', 'Technology'),
('00000000-0000-0000-0000-000000000002', 'HEALTHCARE', 'Healthcare'),
('00000000-0000-0000-0000-000000000003', 'FINANCIALS', 'Financials'),
('00000000-0000-0000-0000-000000000004', 'CONSUMER', 'Consumer'),
('00000000-0000-0000-0000-000000000005', 'INDUSTRIALS', 'Industrials'),
('00000000-0000-0000-0000-000000000006', 'ENERGY', 'Energy'),
('00000000-0000-0000-0000-000000000007', 'UTILITIES', 'Utilities'),
('00000000-0000-0000-0000-000000000008', 'MATERIALS', 'Materials'),
('00000000-0000-0000-0000-000000000009', 'REAL_ESTATE', 'Real estate'),
('00000000-0000-0000-0000-000000000010', 'COMMUNICATIONS', 'Communications'),
('00000000-0000-0000-0000-000000000011', 'OTHER', 'Other');

-- ============================================================================
-- 7. ASSETS
-- ============================================================================

CREATE TABLE assets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    symbol VARCHAR(40),
    type VARCHAR(24) NOT NULL
        CHECK (type IN ('STOCK','CRYPTO','FUND','ETF','REAL_ESTATE','OTHER')),
    sector_id UUID REFERENCES asset_sectors(id) ON DELETE RESTRICT,
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    created_at TIMESTAMPTZ NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_assets_id_user UNIQUE (id, user_id)
);

CREATE INDEX idx_assets_user_active
    ON assets(user_id, active, name);
CREATE INDEX idx_assets_user_sector
    ON assets(user_id, sector_id);
CREATE INDEX idx_assets_user_currency
    ON assets(user_id, currency);

-- ============================================================================
-- 8. ACTIVITIES - SOURCE OF TRUTH LEDGER
-- ============================================================================

CREATE TABLE activities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_id UUID,
    type VARCHAR(24) NOT NULL
        CHECK (type IN ('BUY','SELL','DIVIDEND','INTEREST','DEPOSIT','WITHDRAW','SPLIT','EXCHANGE','OPENING_CASH')),
    occurred_at TIMESTAMPTZ NOT NULL,
    ordering_key BIGINT NOT NULL CHECK (ordering_key >= 0),
    quantity DECIMAL(28,12),
    unit_price DECIMAL(28,12),
    amount DECIMAL(28,12),
    currency CHAR(3),
    secondary_amount DECIMAL(28,12),
    secondary_currency CHAR(3),
    ratio DECIMAL(28,16),
    local_currency CHAR(3),
    fx_rate_to_local DECIMAL(28,16),
    fx_rate_source VARCHAR(100),
    fx_rate_timestamp TIMESTAMPTZ,
    amount_in_local DECIMAL(28,12),
    comment VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_activities_id_user UNIQUE (id, user_id),
    CONSTRAINT uq_activities_id_user_type UNIQUE (id, user_id, type),
    CONSTRAINT uq_activities_id_user_asset_type UNIQUE (id, user_id, asset_id, type),
    CONSTRAINT uq_activity_order UNIQUE (user_id, occurred_at, ordering_key),

    CONSTRAINT fk_activity_asset_user
        FOREIGN KEY(asset_id, user_id)
        REFERENCES assets(id, user_id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_activity_currency
        CHECK (currency IS NULL OR currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_activity_secondary_currency
        CHECK (secondary_currency IS NULL OR secondary_currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_activity_local_currency
        CHECK (local_currency IS NULL OR local_currency ~ '^[A-Z]{3}$'),

    CONSTRAINT chk_activity_cash_fields
        CHECK (
            type NOT IN ('BUY','SELL','DIVIDEND','INTEREST','DEPOSIT','WITHDRAW','OPENING_CASH')
            OR (amount > 0 AND currency IS NOT NULL)
        ),

    CONSTRAINT chk_activity_trade_fields
        CHECK (
            type NOT IN ('BUY','SELL')
            OR (asset_id IS NOT NULL AND quantity > 0 AND unit_price > 0)
        ),

    CONSTRAINT chk_activity_split_fields
        CHECK (
            type <> 'SPLIT'
            OR (asset_id IS NOT NULL AND ratio > 0)
        ),

    CONSTRAINT chk_activity_exchange_fields
        CHECK (
            type <> 'EXCHANGE'
            OR (
                amount > 0
                AND currency IS NOT NULL
                AND secondary_amount > 0
                AND secondary_currency IS NOT NULL
                AND currency <> secondary_currency
                AND ratio > 0
            )
        ),

    CONSTRAINT chk_activity_local_fx
        CHECK (
            type NOT IN ('BUY','SELL','DIVIDEND','INTEREST','DEPOSIT','WITHDRAW','OPENING_CASH')
            OR (
                local_currency IS NOT NULL
                AND fx_rate_to_local > 0
                AND fx_rate_source IS NOT NULL
                AND fx_rate_timestamp IS NOT NULL
                AND amount_in_local > 0
            )
        )
);

CREATE INDEX idx_activities_user_time
    ON activities(user_id, occurred_at, ordering_key, id);
CREATE INDEX idx_activities_asset_time
    ON activities(user_id, asset_id, occurred_at, ordering_key);
CREATE INDEX idx_activities_type_time
    ON activities(user_id, type, occurred_at);

-- ============================================================================
-- 9. ACTIVITY AUDIT
-- ============================================================================

CREATE TABLE activity_audit (
    id UUID PRIMARY KEY,
    activity_id UUID NOT NULL,
    user_id UUID NOT NULL,
    before_data JSONB NOT NULL,
    after_data JSONB NOT NULL,
    reason VARCHAR(500),
    changed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_activity_audit_activity_user
        FOREIGN KEY (activity_id, user_id)
        REFERENCES activities(id, user_id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_activity_audit_activity
    ON activity_audit(user_id, activity_id, changed_at);

-- ============================================================================
-- 10. HOLDING SNAPSHOTS
-- ============================================================================

CREATE TABLE holdings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_id UUID NOT NULL,
    snapshot_at TIMESTAMPTZ NOT NULL,
    quantity DECIMAL(28,12) NOT NULL CHECK (quantity > 0),
    acquisition_cost DECIMAL(28,12) NOT NULL CHECK (acquisition_cost >= 0),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    history_incomplete BOOLEAN NOT NULL DEFAULT TRUE,
    superseded BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_holdings_id_user_asset UNIQUE (id, user_id, asset_id),
    CONSTRAINT fk_holding_asset_user
        FOREIGN KEY(asset_id, user_id)
        REFERENCES assets(id, user_id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_holdings_user_asset_snapshot
    ON holdings(user_id, asset_id, snapshot_at)
    WHERE superseded = FALSE;

-- ============================================================================
-- 11. DERIVED FIFO LOTS
-- ============================================================================

CREATE TABLE lots (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_id UUID NOT NULL,
    source_activity_id UUID,
    source_holding_id UUID,
    acquired_at TIMESTAMPTZ NOT NULL,
    acquisition_order BIGINT NOT NULL CHECK (acquisition_order >= 0),
    original_quantity DECIMAL(28,12) NOT NULL CHECK (original_quantity > 0),
    remaining_quantity DECIMAL(28,12) NOT NULL
        CHECK (remaining_quantity >= 0 AND remaining_quantity <= original_quantity),
    total_cost DECIMAL(28,12) NOT NULL CHECK (total_cost >= 0),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    synthetic BOOLEAN NOT NULL DEFAULT FALSE,

    -- Persistence-only discriminator used to make the provenance FK express
    -- that a non-synthetic lot can only originate from a BUY activity.
    source_activity_type VARCHAR(24) NOT NULL DEFAULT 'BUY',

    CONSTRAINT uq_lots_id_user UNIQUE (id, user_id),

    CONSTRAINT chk_lot_source_mode
        CHECK (
            (source_activity_id IS NOT NULL
             AND source_holding_id IS NULL
             AND synthetic = FALSE
             AND source_activity_type = 'BUY')
            OR
            (source_activity_id IS NULL
             AND source_holding_id IS NOT NULL
             AND synthetic = TRUE
             AND source_activity_type = 'BUY')
        ),

    CONSTRAINT fk_lot_asset_user
        FOREIGN KEY(asset_id, user_id)
        REFERENCES assets(id, user_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_lot_source_buy
        FOREIGN KEY (source_activity_id, user_id, asset_id, source_activity_type)
        REFERENCES activities (id, user_id, asset_id, type)
        ON DELETE RESTRICT,

    CONSTRAINT fk_lot_source_holding_user_asset
        FOREIGN KEY (source_holding_id, user_id, asset_id)
        REFERENCES holdings (id, user_id, asset_id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_lots_open_fifo
    ON lots(user_id, asset_id, acquired_at, acquisition_order, id)
    WHERE remaining_quantity > 0;

-- ============================================================================
-- 12. DERIVED LOT CONSUMPTIONS
-- ============================================================================

CREATE TABLE lot_consumptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sell_activity_id UUID NOT NULL,
    lot_id UUID NOT NULL,
    quantity DECIMAL(28,12) NOT NULL CHECK (quantity > 0),
    cost_basis DECIMAL(28,12) NOT NULL CHECK (cost_basis >= 0),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),

    -- Persistence-only discriminator used by the provenance FK.
    sell_activity_type VARCHAR(24) NOT NULL DEFAULT 'SELL',

    CONSTRAINT uq_lot_consumption_sell_lot
        UNIQUE(sell_activity_id, lot_id),

    CONSTRAINT fk_consumption_sell_activity
        FOREIGN KEY (sell_activity_id, user_id, sell_activity_type)
        REFERENCES activities (id, user_id, type)
        ON DELETE RESTRICT,

    CONSTRAINT fk_consumption_lot_user
        FOREIGN KEY (lot_id, user_id)
        REFERENCES lots (id, user_id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_lot_consumptions_lot
    ON lot_consumptions(user_id, lot_id);
CREATE INDEX idx_lot_consumptions_sell
    ON lot_consumptions(user_id, sell_activity_id, lot_id);

-- ============================================================================
-- 13. MARKET DATA
-- ============================================================================

CREATE TABLE asset_prices (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_id UUID NOT NULL,
    price_timestamp TIMESTAMPTZ NOT NULL,
    open DECIMAL(28,12),
    high DECIMAL(28,12),
    low DECIMAL(28,12),
    close DECIMAL(28,12),
    adjusted_close DECIMAL(28,12),
    volume DECIMAL(32,12),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    source VARCHAR(100) NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL,

    CHECK (open IS NULL OR open >= 0),
    CHECK (high IS NULL OR high >= 0),
    CHECK (low IS NULL OR low >= 0),
    CHECK (close IS NULL OR close >= 0),
    CHECK (adjusted_close IS NULL OR adjusted_close >= 0),
    CHECK (volume IS NULL OR volume >= 0),

    CONSTRAINT fk_asset_price_asset_user
        FOREIGN KEY(asset_id, user_id)
        REFERENCES assets(id, user_id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_asset_price_asset_timestamp_source
        UNIQUE(asset_id, price_timestamp, source)
);

CREATE INDEX idx_asset_prices_lookup
    ON asset_prices(user_id, asset_id, price_timestamp DESC);

CREATE TABLE fx_rates (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    base_currency CHAR(3) NOT NULL CHECK (base_currency ~ '^[A-Z]{3}$'),
    quote_currency CHAR(3) NOT NULL CHECK (quote_currency ~ '^[A-Z]{3}$'),
    rate_timestamp TIMESTAMPTZ NOT NULL,
    rate DECIMAL(28,16) NOT NULL CHECK (rate > 0),
    source VARCHAR(100) NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL,

    CHECK (base_currency <> quote_currency)
);

CREATE UNIQUE INDEX uq_fx_rates_shared_source
    ON fx_rates(base_currency, quote_currency, rate_timestamp, source)
    WHERE user_id IS NULL;

CREATE UNIQUE INDEX uq_fx_rates_user_source
    ON fx_rates(base_currency, quote_currency, rate_timestamp, source, user_id)
    WHERE user_id IS NOT NULL;

CREATE INDEX idx_fx_rates_lookup
    ON fx_rates(base_currency, quote_currency, rate_timestamp DESC);

CREATE INDEX idx_fx_rates_user_lookup
    ON fx_rates(user_id, base_currency, quote_currency, rate_timestamp DESC)
    WHERE user_id IS NOT NULL;

-- ============================================================================
-- 14. USER LOCAL CURRENCY HISTORY
-- ============================================================================

CREATE TABLE user_local_currency_history (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    valid_from TIMESTAMPTZ NOT NULL,
    valid_to TIMESTAMPTZ,
    inferred BOOLEAN NOT NULL DEFAULT FALSE,
    CHECK (valid_to IS NULL OR valid_to > valid_from)
);

CREATE INDEX idx_user_currency_time
    ON user_local_currency_history(user_id, valid_from, valid_to);

CREATE UNIQUE INDEX uq_user_currency_open_period
    ON user_local_currency_history(user_id)
    WHERE valid_to IS NULL;

-- ============================================================================
-- 15. INVESTMENT ACTIVITY IDEMPOTENCY
-- ============================================================================

CREATE TABLE investment_activity_requests (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    idempotency_key VARCHAR(128) NOT NULL
        CHECK (length(trim(idempotency_key)) BETWEEN 1 AND 128),
    request_hash CHAR(64) NOT NULL CHECK (request_hash ~ '^[0-9a-f]{64}$'),
    activity_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (user_id, idempotency_key),
    UNIQUE (activity_id),

    CONSTRAINT fk_activity_request_activity_user
        FOREIGN KEY (activity_id, user_id)
        REFERENCES activities(id, user_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- 16. TRANSACTIONS <-> INVESTMENTS CROSS-MODULE INTEGRITY
-- ============================================================================

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS chk_transaction_type,
    ADD CONSTRAINT chk_transaction_type
        CHECK (type IN ('INCOME','EXPENSE','TRANSFER_IN','TRANSFER_OUT')),
    ALTER COLUMN amount TYPE DECIMAL(18,8),
    ADD COLUMN currency CHAR(3),
    ADD COLUMN investment_activity_id UUID,
    ADD COLUMN investment_time_zone VARCHAR(100),
    ADD CONSTRAINT chk_transaction_currency
        CHECK (currency IS NULL OR currency ~ '^[A-Z]{3}$'),
    ADD CONSTRAINT chk_investment_transfer_link
        CHECK (
            (
                type IN ('TRANSFER_IN','TRANSFER_OUT')
                AND investment_activity_id IS NOT NULL
                AND currency IS NOT NULL
                AND investment_time_zone IS NOT NULL
            )
            OR
            (
                type NOT IN ('TRANSFER_IN','TRANSFER_OUT')
                AND investment_activity_id IS NULL
                AND investment_time_zone IS NULL
            )
        ),
    ADD CONSTRAINT fk_transaction_investment_activity
        FOREIGN KEY(investment_activity_id, user_id)
        REFERENCES activities(id, user_id)
        ON DELETE RESTRICT;

CREATE UNIQUE INDEX uq_transaction_investment_activity
    ON transactions(investment_activity_id)
    WHERE investment_activity_id IS NOT NULL;

-- ============================================================================
-- 17. DATABASE INTEGRITY FUNCTIONS / TRIGGERS
-- ============================================================================

-- --------------------------------------------------------------------------
-- 17.1 FIFO consumption total cannot exceed the source lot quantity.
-- --------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION enforce_lot_consumption_total() RETURNS trigger
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
     WHERE id = NEW.lot_id
       AND user_id = NEW.user_id
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

-- --------------------------------------------------------------------------
-- 17.2 Local-currency periods cannot overlap for the same user.
-- --------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION enforce_user_currency_period_nonoverlap() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM pg_advisory_xact_lock(
        hashtextextended('user-local-currency:' || NEW.user_id::text, 0)
    );

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

-- --------------------------------------------------------------------------
-- 17.3 A linked investment transfer must match the Activity's local FX data.
-- --------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION validate_investment_transaction_link() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    activity_type VARCHAR(24);
    activity_local_currency CHAR(3);
    activity_amount_in_local DECIMAL(28,12);
BEGIN
    IF NEW.investment_activity_id IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT type, local_currency, amount_in_local
      INTO activity_type, activity_local_currency, activity_amount_in_local
      FROM activities
     WHERE id = NEW.investment_activity_id
       AND user_id = NEW.user_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Investment transfer Activity must belong to the same user'
            USING ERRCODE = '23503';
    END IF;

    IF (NEW.type = 'TRANSFER_OUT' AND activity_type <> 'DEPOSIT')
       OR (NEW.type = 'TRANSFER_IN' AND activity_type <> 'WITHDRAW') THEN
        RAISE EXCEPTION 'TRANSFER_OUT must link to DEPOSIT and TRANSFER_IN must link to WITHDRAW'
            USING ERRCODE = '23514';
    END IF;

    IF NEW.currency IS DISTINCT FROM activity_local_currency
       OR NEW.amount IS DISTINCT FROM activity_amount_in_local THEN
        RAISE EXCEPTION 'Investment transfer amount and currency must match the Activity local FX values'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validate_investment_transaction_link
    BEFORE INSERT OR UPDATE
    ON transactions
    FOR EACH ROW EXECUTE FUNCTION validate_investment_transaction_link();

-- --------------------------------------------------------------------------
-- 17.4 Investment-linked Transactions are immutable.
-- --------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION protect_investment_transactions() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        IF OLD.investment_activity_id IS NOT NULL THEN
            RAISE EXCEPTION 'Investment-linked transactions are immutable'
                USING ERRCODE = '23514';
        END IF;
        RETURN OLD;
    END IF;

    IF OLD.investment_activity_id IS NOT NULL
       OR NEW.investment_activity_id IS NOT NULL THEN
        RAISE EXCEPTION 'Investment-linked transactions are immutable'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_protect_investment_transactions
    BEFORE UPDATE OR DELETE ON transactions
    FOR EACH ROW EXECUTE FUNCTION protect_investment_transactions();

-- --------------------------------------------------------------------------
-- 17.5 Financial fields of a cash Activity become immutable once linked.
-- --------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION protect_linked_cash_activity() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM transactions t
         WHERE t.investment_activity_id = OLD.id
           AND t.user_id = OLD.user_id
    ) AND ROW(
        NEW.user_id, NEW.type, NEW.occurred_at, NEW.ordering_key, NEW.amount,
        NEW.currency, NEW.local_currency, NEW.fx_rate_to_local,
        NEW.fx_rate_source, NEW.fx_rate_timestamp, NEW.amount_in_local
    ) IS DISTINCT FROM ROW(
        OLD.user_id, OLD.type, OLD.occurred_at, OLD.ordering_key, OLD.amount,
        OLD.currency, OLD.local_currency, OLD.fx_rate_to_local,
        OLD.fx_rate_source, OLD.fx_rate_timestamp, OLD.amount_in_local
    ) THEN
        RAISE EXCEPTION 'Financial fields of a cash Activity with a daily transfer are immutable'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_protect_linked_cash_activity
    BEFORE UPDATE ON activities
    FOR EACH ROW EXECUTE FUNCTION protect_linked_cash_activity();

-- --------------------------------------------------------------------------
-- 17.6 DEPOSIT/WITHDRAW must commit together with their daily transfer.
-- --------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION require_cash_activity_transfer() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    expected_transfer_type VARCHAR(24);
BEGIN
    IF NEW.type = 'DEPOSIT' THEN
        expected_transfer_type := 'TRANSFER_OUT';
    ELSIF NEW.type = 'WITHDRAW' THEN
        expected_transfer_type := 'TRANSFER_IN';
    ELSE
        RETURN NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM transactions t
         WHERE t.investment_activity_id = NEW.id
           AND t.user_id = NEW.user_id
           AND t.type = expected_transfer_type
           AND t.currency = NEW.local_currency
           AND t.amount = NEW.amount_in_local
    ) THEN
        RAISE EXCEPTION 'DEPOSIT and WITHDRAW Activities must commit with their matching daily transfer'
            USING ERRCODE = '23514';
    END IF;

    RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER trg_require_cash_activity_transfer
    AFTER INSERT OR UPDATE ON activities
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION require_cash_activity_transfer();

-- ============================================================================
-- 15. INVESTMENT TRACKING SETTINGS
-- ============================================================================

CREATE TABLE investment_tracking_settings (
    user_id UUID PRIMARY KEY
        REFERENCES users(id) ON DELETE CASCADE,

    tracking_start_at TIMESTAMPTZ NOT NULL
);

COMMENT ON TABLE investment_tracking_settings IS
    'Configuración del inicio del período en el que Investments gestiona el Investment Cash del usuario.';

COMMENT ON COLUMN investment_tracking_settings.tracking_start_at IS
    'Instante desde el que las Activities comienzan a afectar al Investment Cash gestionado por Millete.';