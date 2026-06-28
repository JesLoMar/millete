# =========================================================================
# FASE 1: Compilación del Backend (JDK 25 nativo + Maven manual)
# =========================================================================
FROM eclipse-temurin:25-jdk-jammy AS builder

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:resolve -B

COPY src ./src

RUN mvn clean package -DskipTests

# =========================================================================
# FASE 2: Entorno de Ejecución Ligero y Seguro
# =========================================================================
FROM eclipse-temurin:25-jre-jammy

RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

RUN groupadd -r millete -g 1001 && \
    useradd -r -g millete -u 1001 millete

WORKDIR /app

COPY --from=builder --chown=millete:millete /app/target/*.jar app.jar

USER millete

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]