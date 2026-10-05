-- =====================================================================
-- V5 - v0.2.0
-- Esquema definitivo del entorno de prueba para el nuevo modelo
-- de Investments + cambios funcionales generales de la versión 0.2.0
-- =====================================================================


-- =====================================================================
-- 1. CATEGORIES
-- =====================================================================

ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS description VARCHAR(500);

ALTER TABLE categories
    ALTER COLUMN budget_limit TYPE NUMERIC(12, 2);

CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_user_name_active
    ON categories (user_id, LOWER(name))
    WHERE active = TRUE;


-- =====================================================================
-- 2. PLANNED TRANSACTIONS
-- =====================================================================

ALTER TABLE planned_transactions
    ADD COLUMN IF NOT EXISTS failure_count INTEGER NOT NULL DEFAULT 0;

COMMENT ON COLUMN planned_transactions.failure_count IS
    'Número de fallos consecutivos en la ejecución del scheduler. Al llegar a 3, la plantilla se desactiva automáticamente.';


-- =====================================================================
-- 3. GOAL UNITS / INVITATIONS / CONTRIBUTIONS
-- =====================================================================

UPDATE goal_units
SET monthly_target = 0
WHERE monthly_target IS NULL;

ALTER TABLE goal_units
    ALTER COLUMN monthly_target SET NOT NULL;


ALTER TABLE goal_invitations
    DROP COLUMN IF EXISTS token;

DROP INDEX IF EXISTS idx_goal_invitations_token;


ALTER TABLE goal_contributions
    ADD COLUMN IF NOT EXISTS type VARCHAR(20) NOT NULL DEFAULT 'DEPOSIT';

ALTER TABLE goal_contributions
    ADD CONSTRAINT chk_contribution_type
        CHECK (type IN ('DEPOSIT', 'WITHDRAWAL'));


-- =====================================================================
-- 4. TIMESTAMP / TIMEZONE NORMALIZATION
-- =====================================================================

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


-- =====================================================================
-- 5. REMOVE OLD INVESTMENT MODEL
-- =====================================================================

DROP TABLE IF EXISTS investments CASCADE;
DROP TABLE IF EXISTS assets CASCADE;


-- =====================================================================
-- 6. ASSET SECTOR CATALOG
-- =====================================================================

CREATE TABLE asset_sectors (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO asset_sectors (
    id,
    code,
    display_name
) VALUES
    ('00000000-0000-0000-0000-000000000001', 'TECHNOLOGY',     'Technology'),
    ('00000000-0000-0000-0000-000000000002', 'HEALTHCARE',     'Healthcare'),
    ('00000000-0000-0000-0000-000000000003', 'FINANCIALS',     'Financials'),
    ('00000000-0000-0000-0000-000000000004', 'CONSUMER',       'Consumer'),
    ('00000000-0000-0000-0000-000000000005', 'INDUSTRIALS',    'Industrials'),
    ('00000000-0000-0000-0000-000000000006', 'ENERGY',         'Energy'),
    ('00000000-0000-0000-0000-000000000007', 'UTILITIES',      'Utilities'),
    ('00000000-0000-0000-0000-000000000008', 'MATERIALS',      'Materials'),
    ('00000000-0000-0000-0000-000000000009', 'REAL_ESTATE',    'Real estate'),
    ('00000000-0000-0000-0000-000000000010', 'COMMUNICATIONS', 'Communications'),
    ('00000000-0000-0000-0000-000000000011', 'OTHER',          'Other');


-- =====================================================================
-- 7. SHARED ASSETS
-- =====================================================================

CREATE TABLE shared_assets (
    id UUID PRIMARY KEY,

    stable_catalog_id VARCHAR(255) NOT NULL UNIQUE,

    name VARCHAR(120) NOT NULL,

    symbol VARCHAR(50),

    type VARCHAR(30) NOT NULL
        CHECK (
            type IN (
                'STOCK',
                'CRYPTO',
                'FUND',
                'ETF',
                'REAL_ESTATE',
                'OTHER'
            )
        ),

    sector_code VARCHAR(100) NOT NULL,

    sector_display_name VARCHAR(120) NOT NULL,

    sector_custom BOOLEAN NOT NULL DEFAULT FALSE,

    currency VARCHAR(3) NOT NULL
        CHECK (currency ~ '^[A-Z]{3}$'),

    created_at TIMESTAMPTZ NOT NULL,

    modified_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_shared_assets_name
    ON shared_assets(name);

CREATE INDEX idx_shared_assets_symbol
    ON shared_assets(symbol);

CREATE INDEX idx_shared_assets_type
    ON shared_assets(type);

CREATE INDEX idx_shared_assets_currency
    ON shared_assets(currency);


-- =====================================================================
-- 8. USER ASSETS
-- =====================================================================

CREATE TABLE user_assets (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,

    name VARCHAR(120) NOT NULL,

    type VARCHAR(30) NOT NULL
        CHECK (
            type IN (
                'STOCK',
                'CRYPTO',
                'FUND',
                'ETF',
                'REAL_ESTATE',
                'OTHER'
            )
        ),

    sector_code VARCHAR(100),

    sector_display_name VARCHAR(120),

    sector_custom BOOLEAN NOT NULL DEFAULT FALSE,

    origin VARCHAR(30) NOT NULL,

    currency VARCHAR(3) NOT NULL
        CHECK (currency ~ '^[A-Z]{3}$'),

    created_at TIMESTAMPTZ NOT NULL,

    modified_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_user_asset_sector_presence
        CHECK (
            sector_code IS NOT NULL
            OR sector_display_name IS NOT NULL
        )
);

CREATE INDEX idx_user_assets_user
    ON user_assets(user_id);

CREATE INDEX idx_user_assets_user_name
    ON user_assets(user_id, name);


-- =====================================================================
-- 9. INVESTMENT ACTIVITIES
-- =====================================================================

CREATE TABLE investment_activities (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,

    type VARCHAR(30) NOT NULL
        CHECK (
            type IN (
                'BUY',
                'SELL',
                'DIVIDEND',
                'SPLIT',
                'DEPOSIT',
                'WITHDRAW',
                'OPENING_CASH',
                'OPENING_POSITION',
                'EXCHANGE'
            )
        ),

    asset_reference_kind VARCHAR(10),

    asset_reference_id UUID,

    occurred_at TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    modified_at TIMESTAMPTZ NOT NULL,

    ordering_key BIGINT NOT NULL
        CHECK (ordering_key >= 0),

    details JSONB NOT NULL,

    comment VARCHAR(500),

    linked_transaction_id UUID,

    CONSTRAINT uq_investment_activity_order
        UNIQUE (
            user_id,
            occurred_at,
            ordering_key
        ),

    CONSTRAINT chk_investment_activity_asset_reference_kind
        CHECK (
            asset_reference_kind IS NULL
            OR asset_reference_kind IN (
                'SHARED',
                'USER'
            )
        ),

    CONSTRAINT chk_investment_activity_asset_reference_pair
        CHECK (
            (
                asset_reference_kind IS NULL
                AND asset_reference_id IS NULL
            )
            OR
            (
                asset_reference_kind IS NOT NULL
                AND asset_reference_id IS NOT NULL
            )
        )
);

CREATE UNIQUE INDEX uq_investment_activities_id_user
    ON investment_activities(id, user_id);

CREATE INDEX idx_investment_activities_user_time
    ON investment_activities(
        user_id,
        occurred_at,
        ordering_key,
        id
    );

CREATE INDEX idx_investment_activities_user_asset_time
    ON investment_activities(
        user_id,
        asset_reference_kind,
        asset_reference_id,
        occurred_at,
        ordering_key,
        id
    );

CREATE INDEX idx_investment_activities_user_type_time
    ON investment_activities(
        user_id,
        type,
        occurred_at
    );


-- =====================================================================
-- 10. ACTIVITY AUDIT
-- =====================================================================

CREATE TABLE activity_audit (
    id UUID PRIMARY KEY,

    activity_id UUID NOT NULL,

    user_id UUID NOT NULL,

    before_data JSONB NOT NULL,

    after_data JSONB NOT NULL,

    reason VARCHAR(500),

    changed_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_activity_audit_activity_user
        FOREIGN KEY (
            activity_id,
            user_id
        )
        REFERENCES investment_activities (
            id,
            user_id
        )
        ON DELETE RESTRICT
);

CREATE INDEX idx_activity_audit_activity
    ON activity_audit(
        user_id,
        activity_id,
        changed_at
    );


-- =====================================================================
-- 11. HOLDINGS
-- =====================================================================

CREATE TABLE holdings (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,

    asset_reference_kind VARCHAR(10) NOT NULL
        CHECK (
            asset_reference_kind IN (
                'SHARED',
                'USER'
            )
        ),

    asset_reference_id UUID NOT NULL,

    snapshot_at TIMESTAMPTZ NOT NULL,

    quantity DECIMAL(28,12) NOT NULL
        CHECK (quantity > 0),

    acquisition_cost DECIMAL(28,12) NOT NULL
        CHECK (acquisition_cost >= 0),

    acquisition_cost_currency VARCHAR(3) NOT NULL
        CHECK (acquisition_cost_currency ~ '^[A-Z]{3}$'),

    status VARCHAR(20) NOT NULL
        CHECK (
            status IN (
                'ACTIVE',
                'SUPERSEDED'
            )
        ),

    created_at TIMESTAMPTZ NOT NULL,

    superseded_at TIMESTAMPTZ,

    CONSTRAINT uq_holdings_id_user
        UNIQUE (id, user_id)
);

CREATE UNIQUE INDEX uq_holdings_active_user_asset
    ON holdings(
        user_id,
        asset_reference_kind,
        asset_reference_id
    )
    WHERE status = 'ACTIVE';

CREATE INDEX idx_holdings_user_asset_snapshot
    ON holdings(
        user_id,
        asset_reference_kind,
        asset_reference_id,
        snapshot_at
    );

CREATE INDEX idx_holdings_user_status
    ON holdings(
        user_id,
        status
    );


-- =====================================================================
-- 12. LOTS
-- =====================================================================

CREATE TABLE lots (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,

    asset_reference_kind VARCHAR(10) NOT NULL
        CHECK (
            asset_reference_kind IN (
                'SHARED',
                'USER'
            )
        ),

    asset_reference_id UUID NOT NULL,

    source_type VARCHAR(20) NOT NULL
        CHECK (
            source_type IN (
                'ACTIVITY',
                'HOLDING'
            )
        ),

    source_id UUID NOT NULL,

    acquired_at TIMESTAMPTZ NOT NULL,

    acquisition_order BIGINT NOT NULL
        CHECK (acquisition_order >= 0),

    original_quantity DECIMAL(28,12) NOT NULL
        CHECK (original_quantity > 0),

    remaining_quantity DECIMAL(28,12) NOT NULL
        CHECK (
            remaining_quantity >= 0
            AND remaining_quantity <= original_quantity
        ),

    total_cost DECIMAL(28,12) NOT NULL
        CHECK (total_cost >= 0),

    currency VARCHAR(3) NOT NULL
        CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT uq_lots_id_user
        UNIQUE (id, user_id),

    CONSTRAINT uq_lot_source
        UNIQUE (
            user_id,
            source_type,
            source_id
        )
);

CREATE INDEX idx_lots_open_fifo
    ON lots(
        user_id,
        asset_reference_kind,
        asset_reference_id,
        acquired_at,
        acquisition_order,
        id
    )
    WHERE remaining_quantity > 0;

CREATE INDEX idx_lots_user_asset
    ON lots(
        user_id,
        asset_reference_kind,
        asset_reference_id
    );


-- =====================================================================
-- 13. LOT CONSUMPTIONS
-- =====================================================================

CREATE TABLE lot_consumptions (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,

    sell_activity_id UUID NOT NULL,

    lot_id UUID NOT NULL,

    quantity DECIMAL(28,12) NOT NULL
        CHECK (quantity > 0),

    cost_basis DECIMAL(28,12) NOT NULL
        CHECK (cost_basis >= 0),

    currency VARCHAR(3) NOT NULL
        CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT uq_lot_consumption_sell_lot
        UNIQUE (
            sell_activity_id,
            lot_id
        )
);

CREATE INDEX idx_lot_consumptions_lot
    ON lot_consumptions(
        user_id,
        lot_id
    );

CREATE INDEX idx_lot_consumptions_sell
    ON lot_consumptions(
        user_id,
        sell_activity_id,
        lot_id
    );

ALTER TABLE lot_consumptions
    ADD CONSTRAINT fk_lot_consumption_lot
        FOREIGN KEY (
            lot_id,
            user_id
        )
        REFERENCES lots (
            id,
            user_id
        )
        ON DELETE RESTRICT;

ALTER TABLE lot_consumptions
    ADD CONSTRAINT fk_lot_consumption_sell_activity
        FOREIGN KEY (
            sell_activity_id,
            user_id
        )
        REFERENCES investment_activities (
            id,
            user_id
        )
        ON DELETE RESTRICT;


-- =====================================================================
-- 14. POSITIONS
-- =====================================================================

CREATE TABLE positions (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,

    asset_reference_kind VARCHAR(10) NOT NULL
        CHECK (
            asset_reference_kind IN (
                'SHARED',
                'USER'
            )
        ),

    asset_reference_id UUID NOT NULL,

    quantity DECIMAL(28,12) NOT NULL,

    acquisition_cost DECIMAL(28,12) NOT NULL
        CHECK (acquisition_cost >= 0),

    currency VARCHAR(3) NOT NULL
        CHECK (currency ~ '^[A-Z]{3}$'),

    history_incomplete BOOLEAN NOT NULL,

    estimated BOOLEAN NOT NULL,

    CONSTRAINT uq_positions_user_asset
        UNIQUE (
            user_id,
            asset_reference_kind,
            asset_reference_id
        )
);

CREATE INDEX idx_positions_user
    ON positions(user_id);

CREATE INDEX idx_positions_user_asset
    ON positions(
        user_id,
        asset_reference_kind,
        asset_reference_id
    );


-- =====================================================================
-- 15. SHARED ASSET MARKET PRICES
-- =====================================================================

CREATE TABLE asset_prices (
    id UUID PRIMARY KEY,

    shared_asset_id UUID NOT NULL,

    price_timestamp TIMESTAMPTZ NOT NULL,

    open DECIMAL(28,12),

    high DECIMAL(28,12),

    low DECIMAL(28,12),

    close DECIMAL(28,12),

    adjusted_close DECIMAL(28,12),

    volume DECIMAL(32,12),

    currency VARCHAR(3) NOT NULL
        CHECK (currency ~ '^[A-Z]{3}$'),

    source VARCHAR(100) NOT NULL,

    fetched_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_asset_price_shared_asset
        FOREIGN KEY (shared_asset_id)
        REFERENCES shared_assets(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_asset_price_open
        CHECK (open IS NULL OR open >= 0),

    CONSTRAINT chk_asset_price_high
        CHECK (high IS NULL OR high >= 0),

    CONSTRAINT chk_asset_price_low
        CHECK (low IS NULL OR low >= 0),

    CONSTRAINT chk_asset_price_close
        CHECK (close IS NULL OR close >= 0),

    CONSTRAINT chk_asset_price_adjusted_close
        CHECK (
            adjusted_close IS NULL
            OR adjusted_close >= 0
        ),

    CONSTRAINT chk_asset_price_volume
        CHECK (volume IS NULL OR volume >= 0),

    CONSTRAINT uq_asset_price_asset_timestamp_source
        UNIQUE (
            shared_asset_id,
            price_timestamp,
            source
        )
);

CREATE INDEX idx_asset_prices_lookup
    ON asset_prices(
        shared_asset_id,
        price_timestamp DESC
    );


-- =====================================================================
-- 16. USER ASSET PRICES
-- =====================================================================

CREATE TABLE user_asset_prices (
    id UUID PRIMARY KEY,

    user_asset_id UUID NOT NULL,

    unit_price DECIMAL(28,12) NOT NULL
        CHECK (unit_price >= 0),

    currency VARCHAR(3) NOT NULL
        CHECK (currency ~ '^[A-Z]{3}$'),

    price_timestamp TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_user_asset_price_asset
        FOREIGN KEY (user_asset_id)
        REFERENCES user_assets(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_user_asset_prices_asset_timestamp
    ON user_asset_prices(
        user_asset_id,
        price_timestamp DESC,
        id DESC
    );


-- =====================================================================
-- 17. FX RATES
-- =====================================================================

CREATE TABLE fx_rates (
    id UUID PRIMARY KEY,

    base_currency VARCHAR(3) NOT NULL
        CHECK (base_currency ~ '^[A-Z]{3}$'),

    quote_currency VARCHAR(3) NOT NULL
        CHECK (quote_currency ~ '^[A-Z]{3}$'),

    rate_timestamp TIMESTAMPTZ NOT NULL,

    rate DECIMAL(28,16) NOT NULL
        CHECK (rate > 0),

    source VARCHAR(100) NOT NULL,

    fetched_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_fx_different_currencies
        CHECK (
            base_currency <> quote_currency
        ),

    CONSTRAINT uq_fx_rate_source
        UNIQUE (
            base_currency,
            quote_currency,
            rate_timestamp,
            source
        )
);

CREATE INDEX idx_fx_rates_lookup
    ON fx_rates(
        base_currency,
        quote_currency,
        rate_timestamp DESC
    );


-- =====================================================================
-- 18. USER LOCAL CURRENCY HISTORY
-- =====================================================================

CREATE TABLE user_local_currency_history (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,

    currency VARCHAR(3) NOT NULL
        CHECK (currency ~ '^[A-Z]{3}$'),

    valid_from TIMESTAMPTZ NOT NULL,

    valid_to TIMESTAMPTZ,

    inferred BOOLEAN NOT NULL DEFAULT FALSE,

    CHECK (
        valid_to IS NULL
        OR valid_to > valid_from
    )
);

CREATE INDEX idx_user_currency_time
    ON user_local_currency_history(
        user_id,
        valid_from,
        valid_to
    );

CREATE UNIQUE INDEX uq_user_currency_open_period
    ON user_local_currency_history(user_id)
    WHERE valid_to IS NULL;


-- =====================================================================
-- 19. INVESTMENT ACTIVITY IDEMPOTENCY
-- =====================================================================

CREATE TABLE investment_activity_requests (
    user_id UUID NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,

    idempotency_key VARCHAR(128) NOT NULL
        CHECK (
            length(trim(idempotency_key))
            BETWEEN 1 AND 128
        ),

    request_hash CHAR(64) NOT NULL
        CHECK (
            request_hash ~ '^[0-9a-f]{64}$'
        ),

    activity_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (
        user_id,
        idempotency_key
    ),

    UNIQUE (activity_id),

    CONSTRAINT fk_activity_request_activity_user
        FOREIGN KEY (
            activity_id,
            user_id
        )
        REFERENCES investment_activities (
            id,
            user_id
        )
        ON DELETE RESTRICT
);


-- =====================================================================
-- 20. TRANSACTIONS <-> INVESTMENTS
-- =====================================================================

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS chk_transaction_type;

ALTER TABLE transactions
    ADD CONSTRAINT chk_transaction_type
        CHECK (
            type IN (
                'INCOME',
                'EXPENSE',
                'TRANSFER_IN',
                'TRANSFER_OUT'
            )
        );


ALTER TABLE transactions
    ALTER COLUMN amount TYPE DECIMAL(18,8);


ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS currency VARCHAR(3);

ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS investment_activity_id UUID;

ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS investment_time_zone VARCHAR(100);


ALTER TABLE transactions
    ADD CONSTRAINT chk_transaction_currency
        CHECK (
            currency IS NULL
            OR currency ~ '^[A-Z]{3}$'
        );


ALTER TABLE transactions
    ADD CONSTRAINT chk_investment_transfer_link
        CHECK (
            (
                type IN (
                    'TRANSFER_IN',
                    'TRANSFER_OUT'
                )
                AND investment_activity_id IS NOT NULL
                AND currency IS NOT NULL
                AND investment_time_zone IS NOT NULL
            )
            OR
            (
                type NOT IN (
                    'TRANSFER_IN',
                    'TRANSFER_OUT'
                )
                AND investment_activity_id IS NULL
                AND investment_time_zone IS NULL
            )
        );


ALTER TABLE transactions
    ADD CONSTRAINT fk_transaction_investment_activity
        FOREIGN KEY (
            investment_activity_id,
            user_id
        )
        REFERENCES investment_activities (
            id,
            user_id
        )
        ON DELETE RESTRICT;


CREATE UNIQUE INDEX uq_transaction_investment_activity
    ON transactions(investment_activity_id)
    WHERE investment_activity_id IS NOT NULL;


CREATE INDEX idx_transactions_investment_activity
    ON transactions(
        user_id,
        investment_activity_id
    )
    WHERE investment_activity_id IS NOT NULL;


-- =====================================================================
-- 21. INVESTMENT TRACKING SETTINGS
-- =====================================================================

CREATE TABLE investment_tracking_settings (
    user_id UUID PRIMARY KEY
        REFERENCES users(id)
        ON DELETE CASCADE,

    tracking_start_at TIMESTAMPTZ NOT NULL
);

COMMENT ON TABLE investment_tracking_settings IS
    'Configuración del inicio del período en el que Investments gestiona el Investment Cash del usuario.';

COMMENT ON COLUMN investment_tracking_settings.tracking_start_at IS
    'Instante desde el que las Activities comienzan a afectar al Investment Cash gestionado por Millete.';


-- =====================================================================
-- 22. DATABASE INTEGRITY FUNCTIONS
-- =====================================================================


-- ---------------------------------------------------------------------
-- 22.1 Activities: identity is immutable
-- ---------------------------------------------------------------------

CREATE OR REPLACE FUNCTION protect_investment_activity_identity()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.user_id IS DISTINCT FROM OLD.user_id
       OR NEW.type IS DISTINCT FROM OLD.type
       OR NEW.asset_reference_kind IS DISTINCT FROM OLD.asset_reference_kind
       OR NEW.asset_reference_id IS DISTINCT FROM OLD.asset_reference_id
       OR NEW.created_at IS DISTINCT FROM OLD.created_at
       OR NEW.id IS DISTINCT FROM OLD.id
    THEN
        RAISE EXCEPTION
            'La identidad de una Activity es inmutable'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;


CREATE TRIGGER trg_protect_investment_activity_identity
    BEFORE UPDATE
    ON investment_activities
    FOR EACH ROW
    EXECUTE FUNCTION protect_investment_activity_identity();


-- ---------------------------------------------------------------------
-- 22.2 Activities: no physical delete
-- ---------------------------------------------------------------------

CREATE OR REPLACE FUNCTION prevent_investment_activity_delete()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION
        'Las Activities no se pueden eliminar físicamente'
        USING ERRCODE = '23514';
END;
$$;


CREATE TRIGGER trg_prevent_investment_activity_delete
    BEFORE DELETE
    ON investment_activities
    FOR EACH ROW
    EXECUTE FUNCTION prevent_investment_activity_delete();


-- ---------------------------------------------------------------------
-- 22.3 Linked investment transactions are immutable
-- ---------------------------------------------------------------------

CREATE OR REPLACE FUNCTION protect_investment_linked_transaction()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.investment_activity_id IS NOT NULL
       OR NEW.investment_activity_id IS NOT NULL
    THEN
        RAISE EXCEPTION
            'Las Transactions vinculadas a Investments son inmutables'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;


CREATE TRIGGER trg_protect_investment_linked_transaction
    BEFORE UPDATE OR DELETE
    ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION protect_investment_linked_transaction();


-- ---------------------------------------------------------------------
-- 22.4 Linked cash Activity becomes immutable after linkage
-- ---------------------------------------------------------------------

CREATE OR REPLACE FUNCTION protect_linked_investment_activity()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.linked_transaction_id IS NOT NULL
       AND (
            NEW.occurred_at IS DISTINCT FROM OLD.occurred_at
            OR NEW.details IS DISTINCT FROM OLD.details
            OR NEW.comment IS DISTINCT FROM OLD.comment
            OR NEW.linked_transaction_id IS DISTINCT FROM OLD.linked_transaction_id
       )
    THEN
        RAISE EXCEPTION
            'Una Activity vinculada a una Transaction es inmutable'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;


CREATE TRIGGER trg_protect_linked_investment_activity
    BEFORE UPDATE
    ON investment_activities
    FOR EACH ROW
    EXECUTE FUNCTION protect_linked_investment_activity();


-- ---------------------------------------------------------------------
-- 22.5 FIFO total consumption
-- ---------------------------------------------------------------------

CREATE OR REPLACE FUNCTION enforce_lot_consumption_total()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    lot_original_quantity DECIMAL(28,12);
    lot_asset_reference_kind VARCHAR(10);
    lot_asset_reference_id UUID;

    sell_asset_reference_kind VARCHAR(10);
    sell_asset_reference_id UUID;

    sell_activity_type VARCHAR(30);

    already_consumed DECIMAL(28,12);
BEGIN

    IF TG_OP = 'UPDATE'
       AND (
            NEW.lot_id IS DISTINCT FROM OLD.lot_id
            OR NEW.user_id IS DISTINCT FROM OLD.user_id
            OR NEW.sell_activity_id IS DISTINCT FROM OLD.sell_activity_id
       )
    THEN
        RAISE EXCEPTION
            'Los vínculos de una LotConsumption son inmutables'
            USING ERRCODE = '23514';
    END IF;


    SELECT
        original_quantity,
        asset_reference_kind,
        asset_reference_id
    INTO
        lot_original_quantity,
        lot_asset_reference_kind,
        lot_asset_reference_id
    FROM lots
    WHERE id = NEW.lot_id
      AND user_id = NEW.user_id
    FOR UPDATE;


    IF NOT FOUND THEN
        RAISE EXCEPTION
            'El Lot % no pertenece al usuario %',
            NEW.lot_id,
            NEW.user_id
            USING ERRCODE = '23503';
    END IF;


    SELECT
        type,
        asset_reference_kind,
        asset_reference_id
    INTO
        sell_activity_type,
        sell_asset_reference_kind,
        sell_asset_reference_id
    FROM investment_activities
    WHERE id = NEW.sell_activity_id
      AND user_id = NEW.user_id;


    IF NOT FOUND THEN
        RAISE EXCEPTION
            'La LotConsumption debe apuntar a una Activity existente del mismo usuario'
            USING ERRCODE = '23503';
    END IF;


    IF sell_activity_type <> 'SELL' THEN
        RAISE EXCEPTION
            'Una LotConsumption solo puede apuntar a una SELL Activity'
            USING ERRCODE = '23514';
    END IF;


    IF sell_asset_reference_kind IS DISTINCT FROM lot_asset_reference_kind
       OR sell_asset_reference_id IS DISTINCT FROM lot_asset_reference_id
    THEN
        RAISE EXCEPTION
            'El Lot y la SELL Activity deben referirse al mismo Asset'
            USING ERRCODE = '23514';
    END IF;


    SELECT COALESCE(
        SUM(quantity),
        0
    )
    INTO already_consumed
    FROM lot_consumptions
    WHERE lot_id = NEW.lot_id
      AND user_id = NEW.user_id
      AND (
            TG_OP <> 'UPDATE'
            OR id <> NEW.id
      );


    IF already_consumed + NEW.quantity > lot_original_quantity THEN
        RAISE EXCEPTION
            'Las LotConsumptions exceden la cantidad original del Lot %',
            NEW.lot_id
            USING ERRCODE = '23514';
    END IF;


    RETURN NEW;
END;
$$;


CREATE TRIGGER trg_enforce_lot_consumption_total
    BEFORE INSERT OR UPDATE
    ON lot_consumptions
    FOR EACH ROW
    EXECUTE FUNCTION enforce_lot_consumption_total();


-- ---------------------------------------------------------------------
-- 22.6 Local currency periods must not overlap
-- ---------------------------------------------------------------------

CREATE OR REPLACE FUNCTION enforce_user_currency_period_nonoverlap()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN

    PERFORM pg_advisory_xact_lock(
        hashtextextended(
            'user-local-currency:' || NEW.user_id::text,
            0
        )
    );


    IF EXISTS (
        SELECT 1
        FROM user_local_currency_history existing
        WHERE existing.user_id = NEW.user_id
          AND existing.id <> NEW.id
          AND tstzrange(
                existing.valid_from,
                existing.valid_to,
                '[)'
              )
              &&
              tstzrange(
                NEW.valid_from,
                NEW.valid_to,
                '[)'
              )
    )
    THEN
        RAISE EXCEPTION
            'Los períodos de moneda local no pueden solaparse para el usuario %',
            NEW.user_id
            USING ERRCODE = '23P01';
    END IF;


    RETURN NEW;
END;
$$;


CREATE TRIGGER trg_enforce_user_currency_period_nonoverlap
    BEFORE INSERT OR UPDATE
    ON user_local_currency_history
    FOR EACH ROW
    EXECUTE FUNCTION enforce_user_currency_period_nonoverlap();


-- =====================================================================
-- 23. COMMENTS
-- =====================================================================

COMMENT ON TABLE investment_activities IS
    'Ledger único y fuente de verdad de Investments. Las posiciones, lots y cash derivados se reconstruyen mediante replay.';

COMMENT ON TABLE holdings IS
    'Snapshots/restauraciones de holdings. No representan el ledger normal de Activities.';

COMMENT ON TABLE lots IS
    'Proyección derivada de los Lots reconstruidos por PortfolioReplay.';

COMMENT ON TABLE lot_consumptions IS
    'Asignación FIFO de ventas sobre Lots reconstruidos.';

COMMENT ON TABLE positions IS
    'Proyección derivada de la cartera para consultas eficientes.';

COMMENT ON TABLE asset_prices IS
    'Precios históricos de Shared Assets.';

COMMENT ON TABLE user_asset_prices IS
    'Precios históricos introducidos para User Assets.';

COMMENT ON TABLE fx_rates IS
    'Tipos de cambio de mercado compartidos, sin ownership por usuario.';


-- =====================================================================
-- END V5
-- =====================================================================