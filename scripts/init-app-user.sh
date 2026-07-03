
set -e

APP_PASSWORD="${APP_DB_PASSWORD:-$DATABASE_PASSWORD}"

if [ -z "$APP_PASSWORD" ]; then
    echo "ERROR: APP_DB_PASSWORD or DATABASE_PASSWORD environment variable is required"
    exit 1
fi

echo "========================================="
echo "  Creating application user 'millete_app'"
echo "========================================="

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'millete_app') THEN
            CREATE ROLE millete_app WITH LOGIN PASSWORD '${APP_PASSWORD}';
            RAISE NOTICE 'User millete_app created successfully';
        ELSE
            RAISE NOTICE 'User millete_app already exists, skipping creation';
        END IF;
    END
    \$\$;

    GRANT CONNECT ON DATABASE $POSTGRES_DB TO millete_app;
    GRANT USAGE ON SCHEMA public TO millete_app;
    GRANT CREATE ON SCHEMA public TO millete_app;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO millete_app;
    GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO millete_app;

    ALTER DEFAULT PRIVILEGES IN SCHEMA public
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO millete_app;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public
        GRANT USAGE, SELECT ON SEQUENCES TO millete_app;
EOSQL

echo "========================================="
echo "  User 'millete_app' setup complete"
echo "========================================="
