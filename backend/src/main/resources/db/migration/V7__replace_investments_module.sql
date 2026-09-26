-- Rebuild investments from an empty module while preserving every other module.
-- Existing rows in investments are intentionally discarded; no data is migrated.

DROP TABLE IF EXISTS investments CASCADE;

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

CREATE TABLE assets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    symbol VARCHAR(40),
    type VARCHAR(24) NOT NULL CHECK (type IN ('STOCK','CRYPTO','FUND','ETF','REAL_ESTATE','OTHER')),
    sector_id UUID REFERENCES asset_sectors(id) ON DELETE RESTRICT,
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    created_at TIMESTAMPTZ NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (id, user_id)
);
CREATE INDEX idx_assets_user_active ON assets(user_id, active, name);
CREATE INDEX idx_assets_user_sector ON assets(user_id, sector_id);
CREATE INDEX idx_assets_user_currency ON assets(user_id, currency);

CREATE TABLE activities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_id UUID,
    type VARCHAR(24) NOT NULL CHECK (type IN ('BUY','SELL','DIVIDEND','INTEREST','DEPOSIT','WITHDRAW','SPLIT','EXCHANGE','OPENING_CASH')),
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
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_activity_asset_user FOREIGN KEY(asset_id, user_id) REFERENCES assets(id, user_id) ON DELETE RESTRICT,
    CONSTRAINT uq_activity_id_user UNIQUE(id, user_id),
    CONSTRAINT uq_activity_order UNIQUE(user_id, occurred_at, ordering_key),
    CONSTRAINT chk_activity_currency CHECK (currency IS NULL OR currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_activity_secondary_currency CHECK (secondary_currency IS NULL OR secondary_currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_activity_local_currency CHECK (local_currency IS NULL OR local_currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_activity_cash_fields CHECK (
        (type NOT IN ('BUY','SELL','DIVIDEND','INTEREST','DEPOSIT','WITHDRAW','OPENING_CASH')
         OR (amount > 0 AND currency IS NOT NULL))
    ),
    CONSTRAINT chk_activity_trade_fields CHECK (
        (type NOT IN ('BUY','SELL') OR (asset_id IS NOT NULL AND quantity > 0 AND unit_price > 0))
    ),
    CONSTRAINT chk_activity_split_fields CHECK (
        (type <> 'SPLIT' OR (asset_id IS NOT NULL AND ratio > 0))
    ),
    CONSTRAINT chk_activity_exchange_fields CHECK (
        (type <> 'EXCHANGE' OR (amount > 0 AND currency IS NOT NULL AND secondary_amount > 0
          AND secondary_currency IS NOT NULL AND currency <> secondary_currency AND ratio > 0))
    ),
    CONSTRAINT chk_activity_local_fx CHECK (
        (type NOT IN ('BUY','SELL','DIVIDEND','INTEREST','DEPOSIT','WITHDRAW','OPENING_CASH')
         OR (local_currency IS NOT NULL AND fx_rate_to_local > 0 AND fx_rate_source IS NOT NULL
             AND fx_rate_timestamp IS NOT NULL AND amount_in_local > 0))
    )
);
CREATE INDEX idx_activities_user_time ON activities(user_id, occurred_at, ordering_key, id);
CREATE INDEX idx_activities_asset_time ON activities(user_id, asset_id, occurred_at, ordering_key);
CREATE INDEX idx_activities_type_time ON activities(user_id, type, occurred_at);

CREATE TABLE activity_audit (
    id UUID PRIMARY KEY,
    activity_id UUID NOT NULL,
    user_id UUID NOT NULL,
    before_data JSONB NOT NULL,
    after_data JSONB NOT NULL,
    reason VARCHAR(500),
    changed_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (activity_id, user_id) REFERENCES activities(id, user_id) ON DELETE RESTRICT
);
CREATE INDEX idx_activity_audit_activity ON activity_audit(user_id, activity_id, changed_at);

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
    FOREIGN KEY(asset_id, user_id) REFERENCES assets(id, user_id) ON DELETE RESTRICT
);
CREATE INDEX idx_holdings_user_asset_snapshot ON holdings(user_id, asset_id, snapshot_at) WHERE superseded = FALSE;

CREATE TABLE lots (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_id UUID NOT NULL,
    source_activity_id UUID,
    source_holding_id UUID,
    acquired_at TIMESTAMPTZ NOT NULL,
    original_quantity DECIMAL(28,12) NOT NULL CHECK (original_quantity > 0),
    remaining_quantity DECIMAL(28,12) NOT NULL CHECK (remaining_quantity >= 0 AND remaining_quantity <= original_quantity),
    total_cost DECIMAL(28,12) NOT NULL CHECK (total_cost >= 0),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    synthetic BOOLEAN NOT NULL DEFAULT FALSE,
    CHECK ((source_activity_id IS NOT NULL AND source_holding_id IS NULL AND synthetic = FALSE)
        OR (source_activity_id IS NULL AND source_holding_id IS NOT NULL AND synthetic = TRUE)),
    FOREIGN KEY(asset_id, user_id) REFERENCES assets(id, user_id) ON DELETE RESTRICT,
    FOREIGN KEY(source_activity_id, user_id) REFERENCES activities(id, user_id) ON DELETE RESTRICT,
    FOREIGN KEY(source_holding_id) REFERENCES holdings(id) ON DELETE RESTRICT
);
CREATE INDEX idx_lots_open_fifo ON lots(user_id, asset_id, acquired_at, id) WHERE remaining_quantity > 0;

CREATE TABLE lot_consumptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sell_activity_id UUID NOT NULL,
    lot_id UUID NOT NULL,
    quantity DECIMAL(28,12) NOT NULL CHECK (quantity > 0),
    cost_basis DECIMAL(28,12) NOT NULL CHECK (cost_basis >= 0),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    FOREIGN KEY(sell_activity_id, user_id) REFERENCES activities(id, user_id) ON DELETE RESTRICT,
    FOREIGN KEY(lot_id) REFERENCES lots(id) ON DELETE RESTRICT,
    UNIQUE(sell_activity_id, lot_id)
);
CREATE INDEX idx_lot_consumptions_lot ON lot_consumptions(user_id, lot_id);

CREATE TABLE asset_prices (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_id UUID NOT NULL,
    price_timestamp TIMESTAMPTZ NOT NULL,
    open DECIMAL(28,12), high DECIMAL(28,12), low DECIMAL(28,12),
    close DECIMAL(28,12), adjusted_close DECIMAL(28,12), volume DECIMAL(32,12),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    source VARCHAR(100) NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL,
    CHECK (open IS NULL OR open >= 0), CHECK (high IS NULL OR high >= 0),
    CHECK (low IS NULL OR low >= 0), CHECK (close IS NULL OR close >= 0),
    CHECK (adjusted_close IS NULL OR adjusted_close >= 0),
    CHECK (volume IS NULL OR volume >= 0),
    FOREIGN KEY(asset_id, user_id) REFERENCES assets(id, user_id) ON DELETE RESTRICT,
    UNIQUE(asset_id, price_timestamp, source)
);
CREATE INDEX idx_asset_prices_lookup ON asset_prices(user_id, asset_id, price_timestamp DESC);

CREATE TABLE fx_rates (
    id UUID PRIMARY KEY,
    base_currency CHAR(3) NOT NULL CHECK (base_currency ~ '^[A-Z]{3}$'),
    quote_currency CHAR(3) NOT NULL CHECK (quote_currency ~ '^[A-Z]{3}$'),
    rate_timestamp TIMESTAMPTZ NOT NULL,
    rate DECIMAL(28,16) NOT NULL CHECK (rate > 0),
    source VARCHAR(100) NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL,
    CHECK (base_currency <> quote_currency),
    UNIQUE(base_currency, quote_currency, rate_timestamp, source)
);
CREATE INDEX idx_fx_rates_lookup ON fx_rates(base_currency, quote_currency, rate_timestamp DESC);

CREATE TABLE user_local_currency_history (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    valid_from TIMESTAMPTZ NOT NULL,
    valid_to TIMESTAMPTZ,
    inferred BOOLEAN NOT NULL DEFAULT FALSE,
    CHECK (valid_to IS NULL OR valid_to > valid_from)
);
CREATE INDEX idx_user_currency_time ON user_local_currency_history(user_id, valid_from, valid_to);
CREATE UNIQUE INDEX uq_user_currency_open_period ON user_local_currency_history(user_id) WHERE valid_to IS NULL;

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS chk_transaction_type,
    ADD CONSTRAINT chk_transaction_type CHECK (type IN ('INCOME','EXPENSE','TRANSFER_IN','TRANSFER_OUT')),
    ALTER COLUMN amount TYPE DECIMAL(18,8),
    ADD COLUMN currency CHAR(3),
    ADD COLUMN investment_activity_id UUID,
    ADD COLUMN investment_time_zone VARCHAR(100),
    ADD CONSTRAINT chk_transaction_currency CHECK (currency IS NULL OR currency ~ '^[A-Z]{3}$'),
    ADD CONSTRAINT chk_investment_transfer_link CHECK (
        (type IN ('TRANSFER_IN','TRANSFER_OUT') AND investment_activity_id IS NOT NULL AND currency IS NOT NULL AND investment_time_zone IS NOT NULL)
        OR (type NOT IN ('TRANSFER_IN','TRANSFER_OUT') AND investment_activity_id IS NULL AND investment_time_zone IS NULL)
    ),
    ADD CONSTRAINT fk_transaction_investment_activity FOREIGN KEY(investment_activity_id, user_id)
        REFERENCES activities(id, user_id) ON DELETE RESTRICT;
CREATE UNIQUE INDEX uq_transaction_investment_activity ON transactions(investment_activity_id) WHERE investment_activity_id IS NOT NULL;
