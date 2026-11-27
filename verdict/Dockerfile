# Keep the newest Python branch with standard Linux ONNX Runtime wheels.
FROM python:3.14.10-slim-trixie@sha256:22eafb370920ad8b429e0aec3fff433e0669968519f5d60469d83729c138b28b

ENV PYTHONUNBUFFERED=1
ENV HF_HOME=/data/huggingface
ENV HF_HUB_DISABLE_TELEMETRY=1

RUN apt-get update \
    && apt-get install -y --no-install-recommends git \
    && rm -rf /var/lib/apt/lists/*

# httpx is imported at runtime by verdict.models but is not declared in its
# serve/onnx extras upstream, so it must be installed explicitly.
RUN pip install --no-cache-dir \
    "verdictml[serve,onnx] @ git+https://github.com/Manavarya09/verdict@76bc1fe9ba87ec5b746ed33faac48812f1d1a4ca" \
    "httpx>=0.27"

EXPOSE 8002

CMD ["verdict", "serve", "--host", "0.0.0.0", "--port", "8002"]
