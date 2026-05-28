#!/bin/bash
# =============================================
# MILITE - Arranque en desarrollo
# Carga variables del .env y arranca la app
# =============================================

# Cargar variables del .env
set -a
source .env
set +a

# Arrancar Spring Boot
./mvnw spring-boot:run