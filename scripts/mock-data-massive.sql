-- ============================================================================
-- MILLETE - MASSIVE MOCK DATA (single user, for server-side pagination tests)
-- ============================================================================
-- Target schema: V1__initial_schema.sql + V2__v0.1.0.sql (final state,
-- i.e. goal_units / goal_members / goal_invitations / goal_contributions).
--
-- Creates ONE user:
--     username: Chus
--     password: 12345678   (hashed with BCrypt via pgcrypto's crypt())
--
-- Row volumes (adjust the generate_series bounds below to taste):
--     categories            50
--     transactions          200,000   (~3 years of history)
--     planned_transactions  3,000
--     investments           2,000
--     savings_goals         1,500
--     notifications         20,000
--     goal_units            500  (+ 1 member, ~30 contributions,
--                                  ~2 invitations per unit)
--
-- Usage (Docker stack, run as the postgres superuser):
--     docker exec -i millete_db psql -U postgres -d millete < scripts/mock-data-massive.sql
-- or with a local psql:
--     psql -h localhost -U postgres -d millete -f scripts/mock-data-massive.sql
--
-- WARNING: intended for a throwaway/dev database. Run it only once;
-- re-running violates the users.username / users.email unique constraints.
-- To wipe the mock data afterwards:
--     DELETE FROM users WHERE username = 'Chus';   -- cascades everywhere
-- ============================================================================

BEGIN;

-- Speed up bulk inserts inside this session.
SET synchronous_commit = off;

-- BCrypt support for the password hash.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================================
-- 1. USER "Chus"  (password: 12345678, BCrypt $2a$ — Spring BCrypt compatible)
-- ============================================================================
INSERT INTO users (
    id, username, email, password,
    created_at, modified_at, active, anonymized,
    is_premium, license, premium_tier, telegram_chat_id
)
VALUES (
    '00000000-0000-4000-8000-000000000001',
    'Chus',
    'chus@millete.local',
    crypt('12345678', gen_salt('bf', 10)),
    now() - interval '3 years',
    now(),
    TRUE, FALSE,
    TRUE, 'MOCK-LICENSE-0001', 'PRO', NULL
);

INSERT INTO user_preferences (id, user_id, preferences, created_at, modified_at)
VALUES (
    gen_random_uuid(),
    '00000000-0000-4000-8000-000000000001',
    '{"theme": "dark", "locale": "es", "currency": "EUR"}'::jsonb,
    now(), now()
);

INSERT INTO user_sessions (
    id, user_id, channel, telegram_chat_id,
    login_attempts, blocked_until, last_attempt_at,
    active, created_at, modified_at
)
VALUES (
    gen_random_uuid(),
    '00000000-0000-4000-8000-000000000001',
    'WEB', NULL,
    0, NULL, now(),
    TRUE, now(), now()
);

-- ============================================================================
-- 2. CATEGORIES (50) — kept in a temp table so transactions can reference them
-- ============================================================================
CREATE TEMP TABLE mock_categories (
    id  UUID PRIMARY KEY,
    ord INT  NOT NULL UNIQUE
) ON COMMIT DROP;

INSERT INTO mock_categories (id, ord)
SELECT gen_random_uuid(), generate_series(1, 50);

INSERT INTO categories (
    id, user_id, family_id, name, color, budget_limit,
    created_at, modified_at, active
)
SELECT
    g.id,
    '00000000-0000-4000-8000-000000000001',
    NULL,
    'Category ' || g.ord,
    '#' || lpad(to_hex((random() * 16777215)::int), 6, '0'),
    round((random() * 2000)::numeric, 2),
    now() - (random() * interval '3 years'),
    now(),
    TRUE
FROM mock_categories g;

-- ============================================================================
-- 3. TRANSACTIONS (200,000 — the main pagination stress target)
--    ~80% EXPENSE / 20% INCOME, spread over the last 3 years
-- ============================================================================
INSERT INTO transactions (
    id, user_id, category_id, amount, date, type, description,
    created_at, modified_at, active
)
SELECT
    gen_random_uuid(),
    '00000000-0000-4000-8000-000000000001',
    c.id,
    round((random() * 4990 + 10)::numeric, 2),
    now() - (random() * interval '1095 days'),
    CASE WHEN random() < 0.8 THEN 'EXPENSE' ELSE 'INCOME' END,
    'Mock transaction ' || s.n,
    now() - (random() * interval '1095 days'),
    now(),
    TRUE
FROM (
    SELECT n, 1 + floor(random() * 50)::int AS c_ord
    FROM generate_series(1, 200000) AS s(n)
) s
JOIN mock_categories c ON c.ord = s.c_ord;

-- ============================================================================
-- 4. PLANNED TRANSACTIONS (3,000)
-- ============================================================================
INSERT INTO planned_transactions (
    id, user_id, category_id, amount, type, description,
    frequency_type, frequency_interval, start_date, end_date,
    last_executed_date, created_at, modified_at, active
)
SELECT
    gen_random_uuid(),
    '00000000-0000-4000-8000-000000000001',
    c.id,
    round((random() * 1990 + 10)::numeric, 2),
    CASE WHEN random() < 0.7 THEN 'EXPENSE' ELSE 'INCOME' END,
    'Planned mock ' || s.n,
    (ARRAY['DAYS', 'WEEKS', 'MONTHS', 'YEARS'])[1 + floor(random() * 4)::int],
    1 + floor(random() * 6)::int,
    CURRENT_DATE - (random() * 700)::int,
    CASE WHEN random() < 0.3
         THEN CURRENT_DATE + (30 + random() * 700)::int
         ELSE NULL END,
    CASE WHEN random() < 0.5
         THEN CURRENT_DATE - (random() * 30)::int
         ELSE NULL END,
    now() - (random() * interval '2 years'),
    now(),
    TRUE
FROM (
    SELECT n, 1 + floor(random() * 50)::int AS c_ord
    FROM generate_series(1, 3000) AS s(n)
) s
JOIN mock_categories c ON c.ord = s.c_ord;

-- ============================================================================
-- 5. INVESTMENTS (2,000)
-- ============================================================================
INSERT INTO investments (
    id, user_id, asset_name, ticker, quantity,
    purchase_price, current_price, type, purchase_date,
    created_at, modified_at, active
)
SELECT
    gen_random_uuid(),
    '00000000-0000-4000-8000-000000000001',
    'Asset ' || s.n,
    'MCK' || (s.n % 1000),
    round((random() * 1000 + 0.0001)::numeric, 8),
    round((random() * 5000 + 1)::numeric, 2),
    round((random() * 6000 + 1)::numeric, 2),
    (ARRAY['STOCK', 'CRYPTO', 'FUND', 'REAL_ESTATE', 'OTHER'])[1 + floor(random() * 5)::int],
    now() - (random() * interval '5 years'),
    now() - (random() * interval '5 years'),
    now(),
    TRUE
FROM generate_series(1, 2000) AS s(n);

-- ============================================================================
-- 6. SAVINGS GOALS (1,500)
-- ============================================================================
INSERT INTO savings_goals (
    id, user_id, name, target_amount, current_amount,
    deadline, priority, status, link,
    created_at, modified_at, active
)
SELECT
    gen_random_uuid(),
    '00000000-0000-4000-8000-000000000001',
    'Goal ' || s.n,
    round((random() * 49900 + 100)::numeric, 2),
    round((random() * 25000)::numeric, 2),
    CURRENT_DATE + (30 + random() * 1000)::int,
    (ARRAY['LOW', 'MEDIUM', 'HIGH'])[1 + floor(random() * 3)::int],
    (ARRAY['ACTIVE', 'ACTIVE', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED'])[1 + floor(random() * 6)::int],
    CASE WHEN random() < 0.2
         THEN 'https://example.com/item/' || s.n
         ELSE NULL END,
    now() - (random() * interval '2 years'),
    now(),
    TRUE
FROM generate_series(1, 1500) AS s(n);

-- ============================================================================
-- 7. NOTIFICATIONS (20,000)
-- ============================================================================
INSERT INTO notifications (
    id, user_id, type, title, message, metadata,
    read, action_required, actioned_at,
    created_at, expires_at, active
)
SELECT
    gen_random_uuid(),
    '00000000-0000-4000-8000-000000000001',
    CASE WHEN random() < 0.1 THEN 'GOAL_INVITATION' ELSE 'SYSTEM' END,
    'Notification ' || s.n,
    'Mock notification body number ' || s.n,
    jsonb_build_object('seq', s.n, 'source', 'mock-generator'),
    random() < 0.6,
    random() < 0.15,
    CASE WHEN random() < 0.3
         THEN now() - (random() * interval '365 days')
         ELSE NULL END,
    now() - (random() * interval '1095 days'),
    CASE WHEN random() < 0.5
         THEN now() + (random() * interval '365 days')
         ELSE NULL END,
    TRUE
FROM generate_series(1, 20000) AS s(n);

-- ============================================================================
-- 8. GOAL UNITS (500) + MEMBERS + CONTRIBUTIONS (~30/unit) + INVITATIONS (~2/unit)
-- ============================================================================
CREATE TEMP TABLE mock_goal_units (
    id  UUID PRIMARY KEY,
    ord INT  NOT NULL UNIQUE
) ON COMMIT DROP;

INSERT INTO mock_goal_units (id, ord)
SELECT gen_random_uuid(), generate_series(1, 500);

INSERT INTO goal_units (
    id, name, monthly_target, distribution_mode,
    created_at, modified_at, active
)
SELECT
    g.id,
    'Goal Unit ' || g.ord,
    round((random() * 9900 + 100)::numeric, 2),
    (ARRAY['EQUITATIVE', 'PROPORTIONAL', 'CUSTOM'])[1 + floor(random() * 3)::int],
    now() - (random() * interval '2 years'),
    now(),
    TRUE
FROM mock_goal_units g;

-- One member (Chus, ADMIN) per unit — the backend sanitizes members to the
-- owner anyway (see scripts/generate-massive-standalone.mjs).
INSERT INTO goal_members (
    id, goal_id, user_id, role, salary, custom_percentage,
    joined_at, created_at, modified_at, active
)
SELECT
    gen_random_uuid(),
    g.id,
    '00000000-0000-4000-8000-000000000001',
    'ADMIN',
    round((random() * 4000 + 1000)::numeric, 2),
    NULL,
    now() - (random() * interval '2 years'),
    now(),
    now(),
    TRUE
FROM mock_goal_units g;

INSERT INTO goal_contributions (
    id, goal_id, user_id, amount, date,
    created_at, modified_at, active
)
SELECT
    gen_random_uuid(),
    g.id,
    '00000000-0000-4000-8000-000000000001',
    round((random() * 490 + 10)::numeric, 2),
    now() - (random() * interval '2 years'),
    now(),
    now(),
    TRUE
FROM mock_goal_units g
CROSS JOIN generate_series(1, 30) AS s(n);

INSERT INTO goal_invitations (
    id, goal_id, email, token, status, expires_at,
    inviter_user_id, invited_user_id,
    created_at, modified_at, active
)
SELECT
    gen_random_uuid(),
    g.id,
    'invitee' || g.ord || '_' || s.n || '@mock.local',
    'tok_' || g.ord || '_' || s.n || '_' || substr(md5(random()::text), 1, 16),
    (ARRAY['PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED'])[1 + floor(random() * 4)::int],
    now() + interval '7 days',
    '00000000-0000-4000-8000-000000000001',
    NULL,
    now() - (random() * interval '1 year'),
    now(),
    TRUE
FROM mock_goal_units g
CROSS JOIN generate_series(1, 2) AS s(n);

-- ============================================================================
-- 9. REFRESH PLANNER STATISTICS
-- ============================================================================
ANALYZE;

COMMIT;

-- Done. Log in with Chus / 12345678.
