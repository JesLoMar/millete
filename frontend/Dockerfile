# =========================================================================
# ETAPA 1: BUILD (Compila la aplicación React con Node 22 + PNPM)
# =========================================================================
FROM node:22-alpine AS builder
WORKDIR /app

# Instalar pnpm de forma global (versión exacta requerida)
ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable && corepack prepare pnpm@11.3.0 --activate

# Copiar archivos de dependencias primero (Aprovecha la caché de capas de Docker)
COPY package.json pnpm-lock.yaml* ./

# Instalar dependencias congelando el lockfile
RUN pnpm install --frozen-lockfile

# Copiar el resto del código fuente del frontend
COPY . .

# Variables de entorno para el proceso de build de Vite (Inyectadas desde el entorno)
ARG VITE_API_URL
ARG VITE_APP_VERSION

ENV VITE_API_URL=${VITE_API_URL:-/api/v1}
ENV VITE_APP_VERSION=${VITE_APP_VERSION:-0.0.1}

# Construir la aplicación (Vite generará los estáticos en /app/dist)
RUN pnpm run build

# =========================================================================
# ETAPA 2: RUNTIME (Servidor Nginx Rootless seguro para producción)
# =========================================================================
FROM nginx:1.27-alpine

# Instalar utilidades para debugging y healthcheck
RUN apk add --no-cache curl

# Crear usuario no root con un ID explícito por seguridad
RUN addgroup -g 1001 -S millete-group && \
    adduser -S millete-user -u 1001 -G millete-group

# [CORREGIDO] Copiar tu archivo de configuración optimizado en lugar de usar el 'echo'
# Ojo: Asegúrate de que el archivo se llama 'nginx.conf' y está en la raíz de tu carpeta frontend
COPY nginx.conf /etc/nginx/nginx.conf

# -------------------------------------------------------------------------
# INYECCIÓN CLAVE: Ajustes de permisos para ejecución sin privilegios (Rootless)
# -------------------------------------------------------------------------
RUN sed -i '/^pid/d' /etc/nginx/nginx.conf && \
    sed -i '1s/^/pid \/tmp\/nginx.pid;\n/' /etc/nginx/nginx.conf

# Copiar los archivos compilados desde la etapa anterior
COPY --from=builder --chown=millete-user:millete-group /app/dist /usr/share/nginx/html

# Asegurar permisos en directorios temporales que usará Nginx en modo no-root
RUN chown -R millete-user:millete-group /usr/share/nginx/html /var/cache/nginx /var/log/nginx /etc/nginx && \
    touch /tmp/nginx.pid && \
    chown millete-user:millete-group /tmp/nginx.pid && \
    mkdir -p /tmp/client_body /tmp/fastcgi_temp /tmp/proxy_temp /tmp/scgi_temp /tmp/uwsgi_temp && \
    chown -R millete-user:millete-group /tmp

# Cambiar al usuario seguro sin privilegios
USER millete-user

# Cambiamos al 80; tu nginx.config escucha en el puerto 80 (listen 80;)
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]