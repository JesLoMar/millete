# =========================================================================
# FASE 1: Compilación del Backend (JDK 25 nativo + Maven manual)
# =========================================================================
FROM eclipse-temurin:25-jdk-jammy AS builder

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:resolve -B

COPY src ./src

RUN mvn clean package
# =========================================================================
# FASE 2: Entorno de Ejecución Ligero y Seguro
# =========================================================================
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]