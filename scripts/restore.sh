#!/bin/sh

BACKUP_DIR="/backups"

echo "==================================="
echo "  Database Restoration"
echo "==================================="
echo ""

# List the last 10 available backups
echo "Available backups:"
echo "------------------------------"
ls -1t "${BACKUP_DIR}"/*.sql.gz 2>/dev/null | head -10 | nl
echo ""

if [ -z "$(ls -A ${BACKUP_DIR}/*.sql.gz 2>/dev/null)" ]; then
    echo "No backups available in ${BACKUP_DIR}"
    exit 1
fi

# Prompt user for backup selection
echo -n "Enter backup number to restore (or 'q' to quit): "
read SELECTION

if [ "$SELECTION" = "q" ]; then
    echo "Restoration cancelled"
    exit 0
fi

# Validate that input is strictly a number
case "$SELECTION" in
    ''|*[!0-9]*) echo "Invalid selection"; exit 1 ;;
esac

# Resolve the filename from the selected number
BACKUP_FILE=$(ls -1t "${BACKUP_DIR}"/*.sql.gz 2>/dev/null | sed -n "${SELECTION}p")

if [ -z "$BACKUP_FILE" ]; then
    echo "Invalid selection"
    exit 1
fi

echo ""
echo "You are about to restore: $(basename ${BACKUP_FILE})"
echo "WARNING: This will OVERWRITE database '${PGDATABASE}'"
echo -n "Are you sure? (y/N): "
read CONFIRM

if [ "$CONFIRM" != "y" ]; then
    echo "Restoration cancelled"
    exit 0
fi

echo ""
echo "Starting restoration..."

# FIX: Terminate all active connections to the target database before dropping it
echo "[$(date)] Terminating active connections to ${PGDATABASE}..."
PGPASSWORD="${PGPASSWORD}" psql -h "${PGHOST}" -U "${PGUSER}" -d postgres -c \
"SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '${PGDATABASE}' AND pid <> pg_backend_pid();"

# Drop and recreate database
echo "[$(date)] Dropping and recreating database..."
PGPASSWORD="${PGPASSWORD}" psql -h "${PGHOST}" -U "${PGUSER}" -d postgres -c "DROP DATABASE IF EXISTS ${PGDATABASE};"
PGPASSWORD="${PGPASSWORD}" psql -h "${PGHOST}" -U "${PGUSER}" -d postgres -c "CREATE DATABASE ${PGDATABASE} OWNER ${PGUSER};"

# Restore the compressed backup file using a pipeline
echo "[$(date)] Injecting schema and data..."
if gunzip -c "${BACKUP_FILE}" | PGPASSWORD="${PGPASSWORD}" psql -h "${PGHOST}" -U "${PGUSER}" -d "${PGDATABASE}"; then
    echo ""
    echo "Restoration completed successfully"
else
    echo ""
    echo "Restoration failed"
    exit 1
fi