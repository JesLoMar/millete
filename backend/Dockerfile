# =============================================
# ETAPA 1: BUILD (compila la aplicación)
# =============================================
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de Maven y descargar dependencias primero (cacheo)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:resolve -B

# Copiar el código fuente y compilar
COPY src src
RUN ./mvnw package -DskipTests -B

# =============================================
# ETAPA 2: RUNTIME (imagen ligera de ejecución)
# =============================================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Crear usuario no root para seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar el JAR compilado desde la etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Puerto por defecto de Spring Boot
EXPOSE 8080

# La aplicación se inicia con perfil prod por defecto
ENTRYPOINT ["java", "-jar", "app.jar"]