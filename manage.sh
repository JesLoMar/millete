#!/bin/sh

case "$1" in
    start)
        echo "Starting services..."
        docker compose up -d
        ;;
    stop)
        echo "Stopping services (data preserved)..."
        docker compose stop
        ;;
    restart)
        echo "Restarting services..."
        docker compose restart
        ;;
    down)
        echo "Removing containers (data preserved)..."
        docker compose down
        ;;
    status)
        echo "Status:"
        docker compose ps
        echo ""
        echo "Backups:"
        ls -lh backups/ 2>/dev/null | tail -5
        ;;
    logs)
        if [ -z "$2" ]; then
            docker compose logs --tail=50
        else
            docker compose logs --tail=50 "$2"
        fi
        ;;
    backup-now)
        echo "Manual backup..."
        docker compose exec db-backup sh -c '
            FILE="/backups/${PGDATABASE}_manual_$(date +%Y%m%d_%H%M%S).sql.gz"
            PGPASSWORD="${PGPASSWORD}" pg_dump -h ${PGHOST} -U ${PGUSER} -d ${PGDATABASE} | gzip > "$FILE"
            echo "Backup: $(basename $FILE)"
        '
        ;;
    restore)
        docker compose exec db-backup sh /restore.sh
        ;;
    init)
        echo "Initializing project..."
        docker volume create millete_postgres_data 2>/dev/null && echo "Volume created" || echo "Volume already exists"
        echo "Initialization complete. Run: sh manage.sh start"
        ;;
    clean-all)
        echo "WARNING: This will delete EVERYTHING"
        echo -n "Type 'DELETE' to confirm: "
        read CONFIRM
        if [ "$CONFIRM" = "DELETE" ]; then
            docker compose down -v
            docker volume rm millete_postgres_data 2>/dev/null
            rm -f backups/*.sql.gz
            echo "Everything deleted"
        else
            echo "Cancelled"
        fi
        ;;
    *)
        echo "Usage: sh manage.sh [command]"
        echo ""
        echo "Commands:"
        echo "  start       - Start services"
        echo "  stop        - Stop services"
        echo "  restart     - Restart services"
        echo "  down        - Remove containers (data preserved)"
        echo "  status      - Show status and backups"
        echo "  logs [svc]  - Show logs"
        echo "  backup-now  - Manual backup"
        echo "  restore     - Restore backup"
        echo "  init        - Initialize project"
        echo "  clean-all   - Delete EVERYTHING"
        ;;
esac