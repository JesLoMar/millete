#!/bin/bash
# Script para construir la imagen con diferentes configuraciones

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuración por defecto
ENVIRONMENT=${1:-production}
VERSION=${2:-0.0.1}

echo -e "${YELLOW}Construyendo frontend para entorno: ${ENVIRONMENT}${NC}"

case $ENVIRONMENT in
  production)
    echo -e "${GREEN}Build de producción${NC}"
    docker build \
      --build-arg VITE_API_URL=/api/v1 \
      --build-arg VITE_APP_VERSION=$VERSION \
      -t millete-frontend:latest \
      -t millete-frontend:$VERSION \
      .
    ;;
  
  staging)
    echo -e "${GREEN}Build de staging${NC}"
    docker build \
      --build-arg VITE_API_URL=https://staging-api.example.com/api/v1 \
      --build-arg VITE_APP_VERSION=$VERSION-staging \
      -t millete-frontend:staging \
      .
    ;;
  
  development)
    echo -e "${GREEN}Build de desarrollo${NC}"
    docker build \
      --build-arg VITE_API_URL=http://localhost:8080/api/v1 \
      --build-arg VITE_APP_VERSION=$VERSION-dev \
      -t millete-frontend:dev \
      .
    ;;
  
  *)
    echo -e "${RED}Entorno no válido. Usa: production, staging, development${NC}"
    exit 1
    ;;
esac

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ Build completado exitosamente${NC}"
  
  # Mostrar tamaño de la imagen
  docker images millete-frontend --format "table {{.Tag}}\t{{.Size}}"
else
  echo -e "${RED}✗ Error en el build${NC}"
  exit 1
fi