FROM postgres:16.16-alpine3.24@sha256:f2f98726820d0342055351c6e9d36e705f5bfffb3dd53ff556325114b08afb6d

# Preserve the upstream entrypoint and update the shared OpenSSL libraries.
RUN apk add --no-cache \
    'libcrypto3>=3.5.9-r1' \
    'libssl3>=3.5.9-r1'
