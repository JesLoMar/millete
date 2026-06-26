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
        echo "Restarting containers..."
        echo "Note: For configuration or .env changes, use 'sh manage.sh reload' instead."
        docker compose restart
        ;;
    reload)
        echo "Recreating containers with new configuration..."
        docker compose up -d --force-recreate --build
        ;;
    down)
        echo "Removing containers and internal networks (data preserved)..."
        docker compose down
        ;;
    status)
        echo "Container Status:"
        docker compose ps
        echo ""
        echo "Recent Backups:"
        ls -lh backups/ 2>/dev/null | tail -5
        ;;
    logs)
        if [ -z "$2" ]; then
            docker compose logs --tail=50 -f
        else
            docker compose logs --tail=50 -f "$2"
        fi
        ;;
    backup-now)
        echo "Executing manual backup..."
        docker compose exec db-backup sh -c '
            TIMESTAMP=$(date +%Y%m%d_%H%M%S)
            FILE="/backups/${PGDATABASE}_manual_${TIMESTAMP}.sql.gz"
            PGPASSWORD="${PGPASSWORD}" pg_dump -h "${PGHOST}" -U "${PGUSER}" -d "${PGDATABASE}" --no-owner --no-acl | gzip > "$FILE"
            echo "Backup successfully created: $(basename $FILE)"
        '
        ;;
    restore)
        echo "Stopping backend to release active database connections..."
        docker compose stop backend
        echo ""
        docker compose run --rm -it db-backup sh /restore.sh
        RESTORE_EXIT=$?
        echo ""
        if [ $RESTORE_EXIT -eq 0 ]; then
            echo "Restoration successful. Starting backend..."
            docker compose start backend
        else
            echo "ERROR: Restoration failed (exit code: $RESTORE_EXIT)."
            echo "Starting backend anyway to resume service..."
            docker compose start backend
        fi
        ;;
    init)
        echo "Initializing production environment..."
        docker volume create millete_postgres_data 2>/dev/null && echo "✔ Docker volume created." || echo "ℹ Docker volume already exists."
        mkdir -p backups
        if chown 1000:1000 backups 2>/dev/null; then
            echo "✔ Backup folder permissions set up successfully."
        else
            echo "⚠ Warning: Could not set backup folder permissions."
            echo "  Please run: 'sudo chown -R 1000:1000 backups' if backups fail to write."
        fi
        if [ -f "scripts/init-app-user.sh" ]; then
            chmod +x scripts/init-app-user.sh
            echo "✔ Init script permissions set."
        else
            echo "⚠ Warning: scripts/init-app-user.sh not found."
        fi
        echo ""
        echo "Initialization complete. You can now run: sh manage.sh start"
        ;;
    create-app-user)
        echo "Creating application user 'millete_app' in PostgreSQL..."
        docker compose exec postgres sh /docker-entrypoint-initdb.d/01-init-app-user.sh
        ;;
    clean-all)
        echo "WARNING: This operation will permanently delete ALL containers, volumes, and backups."
        echo -n "Type 'DELETE' to confirm destruction: "
        read CONFIRM
        if [ "$CONFIRM" = "DELETE" ]; then
            docker compose down -v
            docker volume rm -f millete_postgres_data 2>/dev/null
            rm -f backups/*.sql.gz
            echo "Destruction complete. Everything has been cleaned up."
        else
            echo "Aborted. No data was harmed."
        fi
        ;;
    *)
        echo "Usage: sh manage.sh [command]"
        echo ""
        echo "Commands:"
        echo "  start           - Start all services in the background"
        echo "  stop            - Stop running services without removing them"
        echo "  restart         - Quickly restart containers (ignores config/.env updates)"
        echo "  reload          - Rebuild images and recreate containers with new configs"
        echo "  down            - Stop and remove containers and networks"
        echo "  status          - Display container health and list recent backups"
        echo "  logs [svc]      - Tail logs (optionally filter by service name)"
        echo "  backup-now      - Trigger an instantaneous manual database backup"
        echo "  restore         - Launch interactive database restoration wizard"
        echo "  init            - Bootstrap volumes and host directory permissions"
        echo "  create-app-user - Create millete_app user in existing database"
        echo "  clean-all       - WIPE everything (Containers, volumes, and backups)"
        ;;
esac