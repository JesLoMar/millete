#!/usr/bin/env bash
# =============================================
# Millete App - Script de Gestión Completa
# =============================================
# Uso: ./start.sh [comando] [opciones]
#
# Comandos principales:
#   start [entorno]    - Inicia la aplicación (por defecto: production)
#   stop               - Detiene la aplicación
#   restart [entorno]  - Reinicia la aplicación
#   build [entorno]    - Solo construye imágenes
#   logs               - Muestra logs en tiempo real
#   clean              - Elimina todo (contenedores, volúmenes, imágenes)
#   status             - Muestra el estado de los servicios
#
# Entornos disponibles:
#   production (default) - API: /api/v1
#   staging              - API: https://staging-api.example.com/api/v1
#   development          - API: http://localhost:8080/api/v1

set -euo pipefail

# =============================================
# CONFIGURACIÓN
# =============================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

PROJECT_NAME="millete"
VERSION="${VERSION:-0.0.1}"
COMPOSE_FILE="docker-compose.yml"

# =============================================
# FUNCIONES DE VERIFICACIÓN
# =============================================

check_requirements() {
    echo -e "${BLUE}[INFO] Verificando requisitos...${NC}"
    
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}[ERROR] Docker no está instalado.${NC}"
        echo -e "${YELLOW}Instálalo desde: https://docs.docker.com/get-docker/${NC}"
        exit 1
    fi
    
    # Detectar versión de Docker Compose
    if docker compose version &> /dev/null; then
        COMPOSE_CMD="docker compose"
        echo -e "${GREEN}[OK] Docker Compose v2 detectado.${NC}"
    elif command -v docker-compose &> /dev/null; then
        COMPOSE_CMD="docker-compose"
        echo -e "${YELLOW}[OK] Docker Compose v1 detectado (considera actualizar a v2).${NC}"
    else
        echo -e "${RED}[ERROR] Docker Compose no está instalado.${NC}"
        exit 1
    fi
    
    # Verificar que Docker está corriendo
    if ! docker info &> /dev/null; then
        echo -e "${RED}[ERROR] Docker no está corriendo. Inícialo primero.${NC}"
        exit 1
    fi
}

create_env_file() {
    if [ ! -f .env.docker ]; then
        echo -e "${YELLOW}[INFO] Creando archivo .env.docker desde plantilla...${NC}"
        
        if [ -f .env.docker.example ]; then
            cp .env.docker.example .env.docker
            echo -e "${GREEN}[OK] Archivo .env.docker creado desde .env.docker.example${NC}"
        else
            # Crear archivo por defecto
            cat > .env.docker << 'EOF'
# =============================================
# Configuración de Base de Datos
# =============================================
DATABASE_NAME=millete_db
DATABASE_USER=millete_user
DATABASE_PASSWORD=cambia_esta_contraseña_123!
DB_PORT=5432

# =============================================
# Backend
# =============================================
APP_PORT=8080
SPRING_PROFILE=docker
LOG_LEVEL=INFO

# =============================================
# Frontend
# =============================================
FRONTEND_PORT=3000
VITE_API_URL=/api/v1
VITE_APP_VERSION=0.0.1

# =============================================
# Seguridad (¡Cámbialos en producción!)
# =============================================
JWT_SECRET=change_this_to_a_random_secret_key_at_least_32_chars
CORS_ALLOWED_ORIGINS=http://localhost:3000
EOF
            echo -e "${GREEN}[OK] Archivo .env.docker creado con valores por defecto.${NC}"
        fi
        
        echo -e "${YELLOW}[AVISO] Revisa y modifica .env.docker antes de continuar.${NC}"
        echo -e "${YELLOW}         Especialmente las contraseñas y secretos.${NC}"
        echo ""
        read -p "¿Quieres continuar con los valores por defecto? (s/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Ss]$ ]]; then
            echo -e "${BLUE}[INFO] Edita .env.docker y vuelve a ejecutar el script.${NC}"
            exit 0
        fi
    fi
}

# =============================================
# FUNCIONES DE BUILD (TU SCRIPT ORIGINAL MEJORADO)
# =============================================

build_frontend() {
    local ENVIRONMENT=${1:-production}
    local BUILD_VERSION=${2:-$VERSION}
    
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}  Construyendo Frontend ($ENVIRONMENT)${NC}"
    echo -e "${YELLOW}========================================${NC}"
    
    cd frontend || exit 1
    
    case $ENVIRONMENT in
        production)
            echo -e "${GREEN}[BUILD] Producción${NC}"
            echo -e "${CYAN}  • API URL: /api/v1${NC}"
            echo -e "${CYAN}  • Versión: $BUILD_VERSION${NC}"
            
            docker build \
                --build-arg VITE_API_URL=/api/v1 \
                --build-arg VITE_APP_VERSION="$BUILD_VERSION" \
                -t millete-frontend:latest \
                -t "millete-frontend:$BUILD_VERSION" \
                .
            ;;
        
        staging)
            echo -e "${GREEN}[BUILD] Staging${NC}"
            echo -e "${CYAN}  • API URL: https://staging-api.example.com/api/v1${NC}"
            echo -e "${CYAN}  • Versión: $BUILD_VERSION-staging${NC}"
            
            docker build \
                --build-arg VITE_API_URL=https://staging-api.example.com/api/v1 \
                --build-arg VITE_APP_VERSION="$BUILD_VERSION-staging" \
                -t millete-frontend:staging \
                -t "millete-frontend:$BUILD_VERSION-staging" \
                .
            ;;
        
        development)
            echo -e "${GREEN}[BUILD] Desarrollo${NC}"
            echo -e "${CYAN}  • API URL: http://localhost:8080/api/v1${NC}"
            echo -e "${CYAN}  • Versión: $BUILD_VERSION-dev${NC}"
            
            docker build \
                --build-arg VITE_API_URL=http://localhost:8080/api/v1 \
                --build-arg VITE_APP_VERSION="$BUILD_VERSION-dev" \
                -t millete-frontend:dev \
                -t "millete-frontend:$BUILD_VERSION-dev" \
                .
            ;;
        
        *)
            echo -e "${RED}[ERROR] Entorno no válido: $ENVIRONMENT${NC}"
            echo -e "${YELLOW}Usa: production, staging, development${NC}"
            cd ..
            return 1
            ;;
    esac
    
    local build_status=$?
    cd ..
    
    if [ $build_status -eq 0 ]; then
        echo -e "${GREEN}✓ Build completado exitosamente${NC}"
        echo ""
        echo -e "${CYAN}Imágenes creadas:${NC}"
        docker images "millete-frontend*" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | head -10
        echo ""
        return 0
    else
        echo -e "${RED}✗ Error en el build${NC}"
        return 1
    fi
}

build_backend() {
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}  Construyendo Backend${NC}"
    echo -e "${YELLOW}========================================${NC}"
    
    cd backend || exit 1
    
    docker build \
        -t millete-backend:latest \
        -t "millete-backend:$VERSION" \
        .
    
    local build_status=$?
    cd ..
    
    if [ $build_status -eq 0 ]; then
        echo -e "${GREEN}✓ Backend construido exitosamente${NC}"
        return 0
    else
        echo -e "${RED}✗ Error en el build del backend${NC}"
        return 1
    fi
}

build_all() {
    local ENVIRONMENT=${1:-production}
    
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  Construyendo toda la aplicación${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    build_backend || return 1
    echo ""
    build_frontend "$ENVIRONMENT" || return 1
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  ¡Build completo exitosamente!${NC}"
    echo -e "${GREEN}========================================${NC}"
}

# =============================================
# FUNCIONES DE GESTIÓN
# =============================================

start_app() {
    local ENVIRONMENT=${1:-production}
    
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  Iniciando $PROJECT_NAME ($ENVIRONMENT)${NC}"
    echo -e "${BLUE}========================================${NC}"
    
    # Actualizar el archivo .env.docker con el entorno
    if [ "$ENVIRONMENT" != "production" ]; then
        echo -e "${YELLOW}[INFO] Configurando variables para entorno: $ENVIRONMENT${NC}"
        # Hacer backup del .env actual
        cp .env.docker .env.docker.backup
        
        case $ENVIRONMENT in
            staging)
                sed -i.bak "s|VITE_API_URL=.*|VITE_API_URL=https://staging-api.example.com/api/v1|" .env.docker
                sed -i.bak "s|VITE_APP_VERSION=.*|VITE_APP_VERSION=$VERSION-staging|" .env.docker
                ;;
            development)
                sed -i.bak "s|VITE_API_URL=.*|VITE_API_URL=http://localhost:8080/api/v1|" .env.docker
                sed -i.bak "s|VITE_APP_VERSION=.*|VITE_APP_VERSION=$VERSION-dev|" .env.docker
                ;;
        esac
        rm -f .env.docker.bak
    fi
    
    # Iniciar con docker-compose
    $COMPOSE_CMD --env-file .env.docker up -d --build
    
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  ¡Aplicación iniciada! 🚀${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo -e "${CYAN}Frontend:${NC}  http://localhost:${FRONTEND_PORT:-3000}"
    echo -e "${CYAN}Backend:${NC}   http://localhost:${APP_PORT:-8080}/api/v1"
    echo -e "${CYAN}Database:${NC}  localhost:${DB_PORT:-5432}"
    echo ""
    echo -e "${YELLOW}Comandos útiles:${NC}"
    echo -e "  Ver logs:     ${GREEN}./start.sh logs${NC}"
    echo -e "  Ver estado:   ${GREEN}./start.sh status${NC}"
    echo -e "  Detener:      ${GREEN}./start.sh stop${NC}"
    echo ""
    
    # Restaurar .env original si se modificó
    if [ -f .env.docker.backup ]; then
        mv .env.docker.backup .env.docker
    fi
}

stop_app() {
    echo -e "${YELLOW}[INFO] Deteniendo $PROJECT_NAME...${NC}"
    $COMPOSE_CMD --env-file .env.docker down
    echo -e "${GREEN}[OK] Aplicación detenida.${NC}"
}

restart_app() {
    local ENVIRONMENT=${1:-production}
    stop_app
    start_app "$ENVIRONMENT"
}

show_logs() {
    local SERVICE=${1:-""}
    echo -e "${BLUE}[INFO] Mostrando logs...${NC}"
    
    if [ -n "$SERVICE" ]; then
        $COMPOSE_CMD --env-file .env.docker logs -f --tail=100 "$SERVICE"
    else
        $COMPOSE_CMD --env-file .env.docker logs -f --tail=100
    fi
}

show_status() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  Estado de los Servicios${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    $COMPOSE_CMD --env-file .env.docker ps
    
    echo ""
    echo -e "${YELLOW}Recursos utilizados:${NC}"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}" \
        millete-frontend millete-backend millete-db 2>/dev/null || true
}

clean_all() {
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}  ⚠️  ADVERTENCIA ⚠️${NC}"
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}Esto eliminará:${NC}"
    echo -e "  • Todos los contenedores"
    echo -e "  • Todos los volúmenes (incluyendo datos de BD)"
    echo -e "  • Todas las imágenes construidas"
    echo ""
    read -p "¿Estás COMPLETAMENTE seguro? Escribe 'ELIMINAR' para confirmar: " -r
    echo
    
    if [ "$REPLY" = "ELIMINAR" ]; then
        echo -e "${RED}[INFO] Procediendo con la limpieza total...${NC}"
        
        # Detener y eliminar contenedores y volúmenes
        $COMPOSE_CMD --env-file .env.docker down -v --rmi all 2>/dev/null || true
        
        # Eliminar imágenes huérfanas
        docker system prune -f --volumes 2>/dev/null || true
        
        # Eliminar imágenes específicas del proyecto
        docker images "millete-*" -q | xargs -r docker rmi -f 2>/dev/null || true
        
        echo -e "${GREEN}[OK] Limpieza completada.${NC}"
    else
        echo -e "${BLUE}[INFO] Operación cancelada.${NC}"
    fi
}

# =============================================
# FUNCIONES DE DESARROLLO
# =============================================

dev_mode() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  Modo Desarrollo (Hot Reload)${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo -e "${YELLOW}[INFO] Iniciando en modo desarrollo...${NC}"
    
    # Iniciar solo servicios de backend
    $COMPOSE_CMD --env-file .env.docker up -d postgres backend
    
    echo -e "${GREEN}[OK] Servicios de backend iniciados.${NC}"
    echo ""
    echo -e "${CYAN}Para el frontend, ejecuta en otra terminal:${NC}"
    echo -e "  ${GREEN}cd frontend && pnpm dev${NC}"
    echo ""
    echo -e "${CYAN}Frontend estará en:${NC} http://localhost:5173"
    echo -e "${CYAN}Backend en:${NC} http://localhost:${APP_PORT:-8080}"
}

# =============================================
# MENSAJE DE AYUDA
# =============================================

show_help() {
    cat << EOF
${GREEN}============================================
  Millete App - Script de Gestión
============================================${NC}

${YELLOW}USO:${NC}
  ./start.sh [comando] [opciones]

${YELLOW}COMANDOS PRINCIPALES:${NC}
  ${GREEN}start [entorno]${NC}     Inicia la aplicación completa
  ${GREEN}stop${NC}               Detiene todos los servicios
  ${GREEN}restart [entorno]${NC}  Reinicia la aplicación
  ${GREEN}build [entorno]${NC}    Solo construye las imágenes
  ${GREEN}logs [servicio]${NC}    Muestra logs (opcional: frontend, backend, postgres)
  ${GREEN}status${NC}             Muestra estado de los servicios
  ${GREEN}clean${NC}              Elimina TODO (¡con confirmación!)
  ${GREEN}dev${NC}                Modo desarrollo (solo backend, frontend manual)

${YELLOW}ENTORNOS DISPONIBLES:${NC}
  ${GREEN}production${NC}  (default)  API: /api/v1
  ${GREEN}staging${NC}                API: https://staging-api.example.com/api/v1
  ${GREEN}development${NC}            API: http://localhost:8080/api/v1

${YELLOW}EJEMPLOS:${NC}
  ./start.sh                          # Inicia en producción
  ./start.sh start development        # Inicia en modo desarrollo
  ./start.sh build staging            # Construye para staging
  ./start.sh logs frontend            # Logs del frontend
  ./start.sh restart production       # Reinicia en producción

${YELLOW}VARIABLES DE ENTORNO:${NC}
  VERSION=1.2.3 ./start.sh build      # Build con versión específica
EOF
}

# =============================================
# MENÚ PRINCIPAL
# =============================================

main() {
    check_requirements
    
    local COMMAND=${1:-start}
    local OPTION=${2:-production}
    
    case $COMMAND in
        start)
            create_env_file
            start_app "$OPTION"
            ;;
        stop)
            stop_app
            ;;
        restart)
            create_env_file
            restart_app "$OPTION"
            ;;
        build)
            create_env_file
            build_all "$OPTION"
            ;;
        logs)
            show_logs "$OPTION"
            ;;
        status)
            show_status
            ;;
        clean)
            clean_all
            ;;
        dev)
            create_env_file
            dev_mode
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo -e "${RED}[ERROR] Comando no reconocido: $COMMAND${NC}"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Manejar Ctrl+C elegantemente
trap 'echo -e "\n${YELLOW}[INFO] Operación interrumpida por el usuario.${NC}"; exit 0' INT

# Ejecutar
main "$@"