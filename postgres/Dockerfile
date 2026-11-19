FROM postgres:16.16-alpine3.24@sha256:1d5b02d3a9941fbd52437db4107f8c52d6297dac553ca1e5c299ebb3756b4857

# Preserve the upstream entrypoint and update the shared OpenSSL libraries.
RUN apk add --no-cache \
    'libcrypto3>=3.5.9-r1' \
    'libssl3>=3.5.9-r1'
