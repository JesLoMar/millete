-- =============================================
-- APPLICATION USER CREATION WITH MINIMAL PRIVILEGES
-- This script runs automatically during PostgreSQL initialization
-- =============================================

-- Create application user (password is pulled from environment variable)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'millete_app') THEN
        CREATE ROLE millete_app WITH LOGIN PASSWORD '${APP_DB_PASSWORD}';
    END IF;
END
$$;

-- Grant connectivity privileges on the database
GRANT CONNECT ON DATABASE millete_db TO millete_app;

-- Grant permissions on the public schema
GRANT USAGE ON SCHEMA public TO millete_app;
GRANT CREATE ON SCHEMA public TO millete_app;

-- Grant CRUD privileges on all existing tables
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO millete_app;

-- Grant permissions on sequences (crucial for auto-incrementing IDs)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO millete_app;

-- Configure default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO millete_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT USAGE, SELECT ON SEQUENCES TO millete_app;