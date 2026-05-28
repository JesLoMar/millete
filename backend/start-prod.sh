#!/bin/bash
# =============================================
# MILETE - Arranque en modo producción (local)
# Carga variables del .env.prod y arranca la app
# =============================================

# Colores para los mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}=============================================${NC}"
echo -e "${YELLOW}  MILETE - MODO PRODUCCIÓN (prueba local)${NC}"
echo -e "${YELLOW}=============================================${NC}"

# Verificar que existe el archivo .env.prod
if [ ! -f .env.prod ]; then
    echo -e "${RED}ERROR: Archivo .env.prod no encontrado${NC}"
    echo -e "Cópialo desde .env.template y renómbralo a .env.prod"
    exit 1
fi

# Cargar variables del .env.prod
set -a
source .env.prod
set +a

# Verificar variables obligatorias
MISSING=false

check_var() {
    if [ -z "${!1}" ]; then
        echo -e "${RED}Falta la variable obligatoria: $1${NC}"
        MISSING=true
    fi
}

echo -e "\n${YELLOW}Verificando variables de entorno...${NC}"

check_var DATABASE_URL
check_var DATABASE_USER
check_var DATABASE_PASSWORD
check_var MAIL_HOST
check_var MAIL_USERNAME
check_var MAIL_PASSWORD
check_var MAIL_FROM
check_var FRONTEND_URL
check_var JWT_SECRET

if [ "$MISSING" = true ]; then
    echo -e "\n${RED}Completa las variables en .env.prod antes de ejecutar${NC}"
    exit 1
fi

echo -e "${GREEN}Todas las variables obligatorias están configuradas${NC}"

# Verificar si PostgreSQL está corriendo
if command -v pg_isready &> /dev/null; then
    if ! pg_isready -q 2>/dev/null; then
        echo -e "${YELLOW}AVISO: PostgreSQL no parece estar corriendo localmente${NC}"
        echo -e "La aplicación fallará si la base de datos no está disponible"
    else
        echo -e "${GREEN}PostgreSQL está corriendo${NC}"
    fi
fi

echo -e "\n${GREEN}Arrancando aplicación en modo PRODUCCIÓN...${NC}"
echo -e "Perfil activo: prod"
echo -e "Base de datos: ${DATABASE_URL}"
echo -e "Frontend: ${FRONTEND_URL}"
echo -e "Logs: WARN (solo advertencias y errores)\n"

./mvnw spring-boot:run -Dspring-boot.run.profiles=prod