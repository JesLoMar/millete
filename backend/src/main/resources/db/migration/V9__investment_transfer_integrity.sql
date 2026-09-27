-- Persist idempotency keys with their Activities so a retried request returns
-- the original cash movement and cannot create another daily transfer.
CREATE TABLE investment_activity_requests (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    idempotency_key VARCHAR(128) NOT NULL
        CHECK (length(trim(idempotency_key)) BETWEEN 1 AND 128),
    request_hash CHAR(64) NOT NULL CHECK (request_hash ~ '^[0-9a-f]{64}$'),
    activity_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, idempotency_key),
    UNIQUE (activity_id),
    FOREIGN KEY (activity_id, user_id)
        REFERENCES activities(id, user_id) ON DELETE RESTRICT
);

-- A linked daily transfer must point to the matching cash Activity and copy
-- the frozen local-currency amount and currency from its recorded FX quote.
CREATE FUNCTION validate_investment_transaction_link() RETURNS trigger
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

-- Daily APIs and imports cannot mutate or remove transactions owned by the
-- investment ledger. Corrections are represented by a new Activity instead.
CREATE FUNCTION protect_investment_transactions() RETURNS trigger
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

-- Once a cash Activity has a daily transfer, only its comment and modified
-- timestamp can change. The transfer's FX context and ordering remain fixed.
CREATE FUNCTION protect_linked_cash_activity() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM transactions t
         WHERE t.investment_activity_id = OLD.id
           AND t.user_id = OLD.user_id
    ) AND ROW(
        NEW.user_id, NEW.type, NEW.occurred_at, NEW.ordering_key, NEW.amount,
        NEW.currency, NEW.local_currency, NEW.fx_rate_to_local,
        NEW.fx_rate_source, NEW.fx_rate_timestamp, NEW.amount_in_local, NEW.active
    ) IS DISTINCT FROM ROW(
        OLD.user_id, OLD.type, OLD.occurred_at, OLD.ordering_key, OLD.amount,
        OLD.currency, OLD.local_currency, OLD.fx_rate_to_local,
        OLD.fx_rate_source, OLD.fx_rate_timestamp, OLD.amount_in_local, OLD.active
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

-- The application creates both rows in one transaction. A deferred check also
-- prevents any other writer from committing a cash Activity on its own.
CREATE FUNCTION require_cash_activity_transfer() RETURNS trigger
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
