#!/bin/sh

# Configuration
BACKUP_DIR="/backups"
RETENTION_DAYS=${BACKUP_RETENTION_DAYS:-7}
LAST_BACKUP=""

echo "==================================="
echo "  Daily Backup System"
echo "==================================="
echo "Database: ${PGDATABASE}"
echo "Host: ${PGHOST}"
echo "User: ${PGUSER}"
echo "Retention: ${RETENTION_DAYS} days"
echo "==================================="

# Check if today's backup already exists (recovery after restart)
TODAY=$(date +%Y%m%d)
EXISTING_BACKUP=$(ls -t "${BACKUP_DIR}/${PGDATABASE}_${TODAY}"*.sql.gz 2>/dev/null | head -1)
if [ -n "$EXISTING_BACKUP" ]; then
    echo "[$(date)] Today's backup already exists: $(basename ${EXISTING_BACKUP})"
    LAST_BACKUP="$TODAY"
fi

while true; do
    HOUR=$(date +%H)
    TODAY=$(date +%Y%m%d)
    
    if [ "$HOUR" = "02" ] && [ "$LAST_BACKUP" != "$TODAY" ]; then
        TIMESTAMP=$(date +'%Y%m%d_%H%M%S')
        BACKUP_FILE="${BACKUP_DIR}/${PGDATABASE}_${TIMESTAMP}.sql.gz"
        
        echo "[$(date)] Starting daily backup..."
        
        if PGPASSWORD="${PGPASSWORD}" pg_dump \
            -h "${PGHOST}" \
            -U "${PGUSER}" \
            -d "${PGDATABASE}" \
            --no-owner \
            --no-acl \
            | gzip > "${BACKUP_FILE}"; then
            
            SIZE=$(ls -lh "${BACKUP_FILE}" | awk '{print $5}')
            echo "[$(date)] Backup successful: $(basename ${BACKUP_FILE}) (${SIZE})"
            LAST_BACKUP="$TODAY"
            
            DELETED=$(find "${BACKUP_DIR}" -name "${PGDATABASE}_*.sql.gz" -mtime +${RETENTION_DAYS} -delete -print | wc -l)
            if [ "$DELETED" -gt 0 ]; then
                echo "[$(date)] Cleaned ${DELETED} old backup(s) (>${RETENTION_DAYS} days)"
            fi
        else
            echo "[$(date)] Backup failed"
        fi
    fi
    sleep 60
done