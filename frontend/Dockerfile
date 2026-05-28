# =============================================
# ETAPA 1: BUILD (compila la aplicación React)
# =============================================
FROM node:22-alpine AS builder

WORKDIR /app

# Instalar pnpm
RUN corepack enable && corepack prepare pnpm@11.3.0 --activate

# Copiar archivos de dependencias primero (cacheo de capas)
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

# Copiar el código fuente
COPY . .

# Variables de entorno para el build
# ARG permite pasar valores durante el build (docker build --build-arg)
ARG VITE_API_URL
ARG VITE_APP_VERSION

# Establecer valores por defecto si no se proporcionan
ENV VITE_API_URL=${VITE_API_URL:-/api/v1}
ENV VITE_APP_VERSION=${VITE_APP_VERSION:-0.0.1}

# Construir la aplicación
RUN pnpm run build

# =============================================
# ETAPA 2: RUNTIME (Nginx para servir estáticos)
# =============================================
FROM nginx:1.27-alpine

# Instalar utilidades adicionales (opcional, para debugging)
RUN apk add --no-cache curl wget

# Crear usuario no root para ejecutar nginx (seguridad)
RUN addgroup -g 1001 -S nginx-user && \
    adduser -S nginx-user -u 1001 -G nginx-user

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Verificar que la configuración de nginx es válida
RUN nginx -t

# Copiar los archivos compilados desde la etapa de build
COPY --from=builder --chown=nginx-user:nginx-user /app/dist /usr/share/nginx/html

# Crear directorios para logs con permisos correctos
RUN touch /var/log/nginx/access.log /var/log/nginx/error.log && \
    chown -R nginx-user:nginx-user /var/log/nginx /var/cache/nginx /var/run && \
    chmod -R 755 /var/log/nginx /var/cache/nginx

# Eliminar archivos innecesarios del html (opcional)
RUN rm -rf /usr/share/nginx/html/*.map 2>/dev/null || true

# Configurar healthcheck mejorado
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost/health || exit 1

# Exponer puerto
EXPOSE 80

# Cambiar a usuario no root para ejecutar nginx
USER nginx-user

# Comando para iniciar nginx
CMD ["nginx", "-g", "daemon off;"]