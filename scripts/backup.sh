#!/bin/sh

# Configuration
BACKUP_DIR="/backups"
RETENTION_DAYS=${BACKUP_RETENTION_DAYS:-7}

echo "==================================="
echo "  Daily Backup System"
echo "==================================="
echo "Database: ${PGDATABASE}"
echo "Host: ${PGHOST}"
echo "Retention: ${RETENTION_DAYS} days"
echo "==================================="

while true; do
    # Get current hour
    HOUR=$(date +%H)
    
    # Backup at 2 AM
    if [ "$HOUR" = "02" ]; then
        TIMESTAMP=$(date +'%Y%m%d_%H%M%S')
        BACKUP_FILE="${BACKUP_DIR}/${PGDATABASE}_${TIMESTAMP}.sql.gz"
        
        echo "[$(date)] Starting daily backup..."
        
        # Perform backup
        if PGPASSWORD="${PGPASSWORD}" pg_dump \
            -h "${PGHOST}" \
            -U "${PGUSER}" \
            -d "${PGDATABASE}" \
            --no-owner \
            --no-acl \
            | gzip > "${BACKUP_FILE}"; then
            
            SIZE=$(ls -lh "${BACKUP_FILE}" | awk '{print $5}')
            echo "[$(date)] Backup successful: $(basename ${BACKUP_FILE}) (${SIZE})"
            
            # Clean old backups
            DELETED=$(find "${BACKUP_DIR}" -name "${PGDATABASE}_*.sql.gz" -mtime +${RETENTION_DAYS} -delete -print | wc -l)
            if [ "$DELETED" -gt 0 ]; then
                echo "[$(date)] Cleaned ${DELETED} old backup(s) (>${RETENTION_DAYS} days)"
            fi
        else
            echo "[$(date)] Backup failed"
        fi
    fi
    
    # Check every minute
    sleep 60
done