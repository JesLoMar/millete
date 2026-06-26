# =========================================================================
# STAGE 1: BUILD (Compile the React application using Node 22 + PNPM)
# =========================================================================
FROM node:22-alpine AS builder
WORKDIR /app

# Install pnpm globally (exact version required)
ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable && corepack prepare pnpm@11.3.0 --activate

# Copy dependency definition files first (Leverages Docker layer caching)
COPY package.json pnpm-lock.yaml* ./

# Install dependencies freezing the lockfile
RUN pnpm install --frozen-lockfile

# Copy the remaining frontend source code
COPY . .

# Environment variables for the Vite build process
ARG VITE_API_URL
ARG VITE_APP_VERSION

ENV VITE_API_URL=${VITE_API_URL:-/api/v1}
ENV VITE_APP_VERSION=${VITE_APP_VERSION:-0.0.1}

# Build the application (Vite outputs static files to /app/dist)
RUN pnpm run build

# =========================================================================
# STAGE 2: RUNTIME (Secure Rootless Nginx Server for Production)
# =========================================================================
FROM nginx:1.27-alpine

# Install utilities for debugging and healthchecking
RUN apk add --no-cache curl

# Create a non-root user with an explicit ID for security
RUN addgroup -g 1001 -S millete-group && \
    adduser -S millete-user -u 1001 -G millete-group

# Copy your optimized, rootless-ready Nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy the compiled static files from the build stage
COPY --from=builder --chown=millete-user:millete-group /app/dist /usr/share/nginx/html

# Ensure proper permissions on temporary directories used by unprivileged Nginx
RUN chown -R millete-user:millete-group /usr/share/nginx/html /var/cache/nginx /var/log/nginx /etc/nginx && \
    touch /tmp/nginx.pid && \
    chown millete-user:millete-group /tmp/nginx.pid && \
    mkdir -p /tmp/client_body /tmp/fastcgi_temp /tmp/proxy_temp /tmp/scgi_temp /tmp/uwsgi_temp && \
    chown -R millete-user:millete-group /tmp

# Switch to the secure unprivileged user
USER millete-user

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]