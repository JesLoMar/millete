FROM python:3.12-slim

ENV PYTHONUNBUFFERED=1
ENV HF_HOME=/data/huggingface
ENV HF_HUB_DISABLE_TELEMETRY=1

RUN apt-get update \
    && apt-get install -y --no-install-recommends git \
    && rm -rf /var/lib/apt/lists/*

# httpx is imported at runtime by verdict.models but is not declared in its
# serve/onnx extras upstream, so it must be installed explicitly.
RUN pip install --no-cache-dir \
    "verdictml[serve,onnx] @ git+https://github.com/Manavarya09/verdict" \
    "httpx>=0.27"

EXPOSE 8002

CMD ["verdict", "serve", "--host", "0.0.0.0", "--port", "8002"]