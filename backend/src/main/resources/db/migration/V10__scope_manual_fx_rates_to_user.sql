ALTER TABLE fx_rates
    ADD COLUMN user_id UUID REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE fx_rates
    DROP CONSTRAINT fx_rates_base_currency_quote_currency_rate_timestamp_source_key;

CREATE UNIQUE INDEX uq_fx_rates_shared_source
    ON fx_rates(base_currency, quote_currency, rate_timestamp, source)
    WHERE user_id IS NULL;

CREATE UNIQUE INDEX uq_fx_rates_user_source
    ON fx_rates(base_currency, quote_currency, rate_timestamp, source, user_id)
    WHERE user_id IS NOT NULL;

CREATE INDEX idx_fx_rates_user_lookup
    ON fx_rates(user_id, base_currency, quote_currency, rate_timestamp DESC)
    WHERE user_id IS NOT NULL;
